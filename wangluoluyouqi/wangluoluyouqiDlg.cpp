// wangluoluyouqiDlg.cpp : implementation file
//

#include "stdafx.h"

#include "wangluoluyouqi.h"
#include "wangluoluyouqiDlg.h"
#define HAVE_REMOTE
#include "pcap.h"
#pragma comment(lib,"wpcap.lib")
#pragma comment(lib,"ws2_32.lib")
#include <afxtempl.h>
#include <afxmt.h>
#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif
#define MAX_IF 5 // ���ӿ���Ŀ
#pragma pack(1)
typedef struct FrameHeader_t { // ֡�ײ�
UCHAR DesMAC[6]; // Ŀ�ĵ�ַ
UCHAR SrcMAC[6]; // Դ��ַ
USHORT FrameType;  // ֡����
} FrameHeader_t;
typedef struct ARPFrame_t { // ARP ֡
FrameHeader_t  FrameHeader; // ֡�ײ�
WORD HardwareType; // Ӳ������
WORD ProtocolType; // Э������
BYTE  HLen; // Ӳ����ַ����
BYTE  PLen; // Э���ַ����
WORD Operation; // ����ֵ
UCHAR SendHa[6]; // Դ MAC ��ַ
ULONG SendIP; // Դ IP ��ַ
UCHAR RecvHa[6]; // Ŀ�� MAC ��ַ
ULONG RecvIP; // Ŀ�� IP ��ַ
} ARPFrame_t;
typedef struct IPHeader_t { // IP �ײ�
BYTE  Ver_HLen; // �汾+ͷ������
BYTE  TOS; // ��������
WORD TotalLen; // �ܳ���
WORD ID; // ��ʶ
WORD Flag_Segment; // ��־+Ƭƫ��
BYTE  TTL; // ����ʱ��
BYTE  Protocol; // Э��
WORD Checksum; // ͷ��У���
ULONG SrcIP; // Դ IP ��ַ
ULONG DstIP; // Ŀ�� IP ��ַ
} IPHeader_t;
typedef struct ICMPHeader_t { // ICMP �ײ�
BYTE Type; // ����
BYTE Code; // ����
WORD Checksum; // У���
WORD Id; // ��ʶ
WORD Sequence; // ���к�
} ICMPHeader_t;
typedef struct IPFrame_t {  // IP ֡
FrameHeader_t  FrameHeader; // ֡�ײ�
IPHeader_t  IPHeader; // IP �ײ�
} IPFrame_t;
typedef struct ip_t { // �����ַ
ULONG IPAddr; // IP ��ַ
ULONG IPMask; // ��������
} ip_t;
typedef struct IfInfo_t {  // �ӿ���Ϣ
CString DeviceName; // �豸��
CString Description; // �豸����
UCHAR MACAddr[6]; // MAC ��ַ
CArray <ip_t,ip_t&>  ip; // IP ��ַ�б�
pcap_t  *adhandle; // pcap ���
} IfInfo_t;
typedef struct SendPacket_t { // �������ݰ��ṹ
int len; // ����
BYTE  PktData[2000];// ���ݻ���
ULONG TargetIP; // Ŀ�� IP ��ַ
UINT_PTR  n_mTimer; // ��ʱ��
UINT  IfNo; // �ӿ����
} SendPacket_t;
typedef struct RouteTable_t { // ·�ɱ�ṹ
ULONG Mask; // ��������
ULONG DstIP; // Ŀ�ĵ�ַ
ULONG NextHop; // ��һ����
UINT  IfNo; // �ӿ����
} RouteTable_t;
typedef struct IP_MAC_t { // IP-MAC ��ַӳ��ṹ
ULONG IPAddr; // IP ��ַ
UCHAR MACAddr[6]; // MAC ��ַ
} IP_MAC_t;
// ȫ�ֱ���
/////////////////////////////////////////////////////////////////////////////
// CAboutDlg dialog used for App About
IfInfo_t IfInfo[MAX_IF]; // �ӿ���Ϣ����
int IfCount; // �ӿڸ���
UINT_PTR TimerCount; // ��ʱ������
CList <SendPacket_t, SendPacket_t&> SP; // �������ݰ��������
CList <IP_MAC_t, IP_MAC_t&> IP_MAC; // IP-MAC ��ַӳ���б�
CList <RouteTable_t, RouteTable_t&> RouteTable; // ·�ɱ�
CWangluoluyouqiDlg *pDlg ; // �Ի���ָ��
CMutex mMutex(0,0,0); // ����
/******************************************************************
*******/
// ȫ�ֺ���
/******************************************************************
*******/
// IP ��ַת��
CString IPntoa(ULONG nIPAddr);
// MAC ��ַת��
CString MACntoa(UCHAR *nMACAddr);
// MAC ��ַ�Ƚ�
bool cmpMAC(UCHAR *MAC1, UCHAR *MAC2);
// MAC ��ַ����
void cpyMAC(UCHAR *MAC1, UCHAR *MAC2);
// MAC ��ַ����
void setMAC(UCHAR *MAC, UCHAR ch);
// IP ��ַ��ѯ
bool IPLookup(ULONG ipaddr, UCHAR *p);
// ���ݰ������߳�
UINT Capture(PVOID pParam);
// ��ȡ���ؽӿ� MAC ��ַ�߳�
UINT CaptureLocalARP(PVOID pParam);
// ���� ARP ����
void ARPRequest(pcap_t *adhandle, UCHAR *srcMAC, ULONG srcIP, ULONG
targetIP);
// ��ѯ·�ɱ�
DWORD RouteLookup(UINT &ifNO, DWORD desIP, CList <RouteTable_t,
RouteTable_t&> *routeTable);
// ���� ARP ���ݰ�
void ARPPacketProc(struct pcap_pkthdr *header, const u_char *pkt_data);
// ���� IP ���ݰ�
void IPPacketProc(IfInfo_t *pIfInfo, struct pcap_pkthdr *header, const u_char
*pkt_data);
// ���� ICMP ���ݰ�
void ICMPPacketProc(IfInfo_t *pIfInfo, BYTE type, BYTE code, const u_char
*pkt_data);
// ��� IP ���ݰ�ͷ��У����Ƿ���ȷ
int IsChecksumRight(char * buffer);
// ����У���
unsigned short ChecksumCompute(unsigned short *buffer, int size);
class CAboutDlg : public CDialog
{
public:
	CAboutDlg();

// Dialog Data
	//{{AFX_DATA(CAboutDlg)
	enum { IDD = IDD_ABOUTBOX };
	//}}AFX_DATA

	// ClassWizard generated virtual function overrides
	//{{AFX_VIRTUAL(CAboutDlg)
	protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	//}}AFX_VIRTUAL

// Implementation
protected:
	//{{AFX_MSG(CAboutDlg)
	//}}AFX_MSG
	DECLARE_MESSAGE_MAP()
};

CAboutDlg::CAboutDlg() : CDialog(CAboutDlg::IDD)
{
	//{{AFX_DATA_INIT(CAboutDlg)
	//}}AFX_DATA_INIT
}

void CAboutDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);

	//{{AFX_DATA_MAP(CAboutDlg)
	//}}AFX_DATA_MAP
}

BEGIN_MESSAGE_MAP(CAboutDlg, CDialog)
	//{{AFX_MSG_MAP(CAboutDlg)
		// No message handlers
	//}}AFX_MSG_MAP
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// CWangluoluyouqiDlg dialog

CWangluoluyouqiDlg::CWangluoluyouqiDlg(CWnd* pParent /*=NULL*/)
	: CDialog(CWangluoluyouqiDlg::IDD, pParent)
{
	//{{AFX_DATA_INIT(CWangluoluyouqiDlg)
		// NOTE: the ClassWizard will add member initialization here
	//}}AFX_DATA_INIT
	// Note that LoadIcon does not require a subsequent DestroyIcon in Win32
	m_hIcon = AfxGetApp()->LoadIcon(IDR_MAINFRAME);
}

void CWangluoluyouqiDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	//{{AFX_DATA_MAP(CWangluoluyouqiDlg)
	DDX_Control(pDX, IDC_IPADDRESS, m_Destination);
	DDX_Control(pDX, IDC_NEXTHOP, m_NextHop);
	DDX_Control(pDX, IDC_MASK, m_Mask);
	DDX_Control(pDX, ROUTER_LIST, m_RouteTable);
	DDX_Control(pDX, LOGGER_LIST, LOGGER);
	//}}AFX_DATA_MAP
}

BEGIN_MESSAGE_MAP(CWangluoluyouqiDlg, CDialog)
	//{{AFX_MSG_MAP(CWangluoluyouqiDlg)
	ON_WM_SYSCOMMAND()
	ON_WM_PAINT()
	ON_WM_QUERYDRAGICON()
	ON_BN_CLICKED(ONSTART_BUTTON, OnStartClickedButton)
	ON_BN_CLICKED(ONSTOP_BUTTON, OnBnClickedButton)
	ON_BN_CLICKED(ADD_ROUTER_BUTTON, OnAddRouterButton)
	ON_BN_CLICKED(DELETE_ROUTER_BUTTON, OnDeleteRouterButton)
	ON_WM_DESTROY()
	ON_WM_TIMER()
	//}}AFX_MSG_MAP
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// CWangluoluyouqiDlg message handlers

BOOL CWangluoluyouqiDlg::OnInitDialog()
{
	CDialog::OnInitDialog();

	// Add "About..." menu item to system menu.

	// IDM_ABOUTBOX must be in the system command range.
	CWangluoluyouqiApp* pApp = (CWangluoluyouqiApp*)AfxGetApp();
	pDlg = (CWangluoluyouqiDlg*)pApp->m_pMainWnd;
	ASSERT((IDM_ABOUTBOX & 0xFFF0) == IDM_ABOUTBOX);
	ASSERT(IDM_ABOUTBOX < 0xF000);

	CMenu* pSysMenu = GetSystemMenu(FALSE);
	if (pSysMenu != NULL)
	{
		CString strAboutMenu;
		strAboutMenu.LoadString(IDS_ABOUTBOX);
		if (!strAboutMenu.IsEmpty())
		{
			pSysMenu->AppendMenu(MF_SEPARATOR);
			pSysMenu->AppendMenu(MF_STRING, IDM_ABOUTBOX, strAboutMenu);
		}
	}

	// Set the icon for this dialog.  The framework does this automatically
	//  when the application's main window is not a dialog
	SetIcon(m_hIcon, TRUE);			// Set big icon
	SetIcon(m_hIcon, FALSE);		// Set small icon
	
	// TODO: Add extra initialization here
	
	return TRUE;  // return TRUE  unless you set the focus to a control
}

void CWangluoluyouqiDlg::OnSysCommand(UINT nID, LPARAM lParam)
{
	if ((nID & 0xFFF0) == IDM_ABOUTBOX)
	{
		CAboutDlg dlgAbout;
		dlgAbout.DoModal();
	}
	else
	{
		CDialog::OnSysCommand(nID, lParam);
	}
}

// If you add a minimize button to your dialog, you will need the code below
//  to draw the icon.  For MFC applications using the document/view model,
//  this is automatically done for you by the framework.

void CWangluoluyouqiDlg::OnPaint() 
{
	if (IsIconic())
	{
		CPaintDC dc(this); // device context for painting

		SendMessage(WM_ICONERASEBKGND, (WPARAM) dc.GetSafeHdc(), 0);

		// Center icon in client rectangle
		int cxIcon = GetSystemMetrics(SM_CXICON);
		int cyIcon = GetSystemMetrics(SM_CYICON);
		CRect rect;
		GetClientRect(&rect);
		int x = (rect.Width() - cxIcon + 1) / 2;
		int y = (rect.Height() - cyIcon + 1) / 2;

		// Draw the icon
		dc.DrawIcon(x, y, m_hIcon);
	}
	else
	{
		CDialog::OnPaint();
	}
}

// The system calls this to obtain the cursor to display while the user drags
//  the minimized window.
HCURSOR CWangluoluyouqiDlg::OnQueryDragIcon()
{
	return (HCURSOR) m_hIcon;
}

void CWangluoluyouqiDlg::OnStartClickedButton() 
{
	MessageBox("666");	
	// TODO: Add your control notification handler code here
	// ��ȡ�����Ľӿ��б�
	pcap_if_t *alldevs, *d;
	pcap_addr_t  *a;
	struct bpf_program fcode;
	char errbuf[PCAP_ERRBUF_SIZE], strbuf[1000];
	int i, j, k;
	ip_t ipaddr;
	UCHAR srcMAC[6];
	ULONG srcIP;
	SetTimer(3999,10000,0);
	// ��ñ������豸�б�
	if (pcap_findalldevs_ex(PCAP_SRC_IF_STRING, NULL ,&alldevs, errbuf) == -1)
	{
	// ���󣬷��ش�����Ϣ
		sprintf(strbuf, "pcap_findalldevs_ex ����: %s", errbuf);
		MessageBox(strbuf);
		PostMessage(WM_QUIT, 0, 0);
	}
		i = 0;
		j = 0;
		k = 0;
		// ��ȡ IP ��ַ��Ϣ
		for(d = alldevs; d != NULL; d = d->next) 
		{ 
			if(d->addresses != NULL  )  // �ų�����modem��Ӱ�� ��û��IP��ַ��
		{
	// �õ�һ����Ч�Ľӿں��� IP ��ַ�б�
		IfInfo[i].DeviceName = d->name;
		IfInfo[i].Description = d->description;
		for(a = d->addresses; a; a = a->next) 
		{
			if (a->addr->sa_family == AF_INET)
			{
				ipaddr.IPAddr  =  (((struct  sockaddr_in*)a->addr)->sin_addr.s_addr);
			ipaddr.IPMask  =  (((struct  sockaddr_in*)a->netmask)->sin_addr.s_addr);
				IfInfo[i].ip.Add(ipaddr);
			j++;
		}
} 
if (i==MAX_IF)  // ��ദ�� MAX_IF ���ӿ�
{
break;
} 
else
{
i++;
}
}
}
// ������·���� IP ��ַ��ĿҪ��
if (j < 2)
{
MessageBox("��·�ɳ���Ҫ�󱾵���������Ӧ���� 2 �� IP ��ַ");
PostMessage(WM_QUIT, 0, 0);
}
// ����ʵ�ʵ�������
IfCount = i; 
// �򿪽ӿ�
for (i=0; i < IfCount; i++)
{
	if ( (IfInfo[i].adhandle = pcap_open(IfInfo[i].DeviceName, // �豸��
65536, // ��������
PCAP_OPENFLAG_PROMISCUOUS,
// ����ģʽ
1000, // ��ʱʱ��
NULL, // Զ����֤
errbuf // ���󻺴�
) ) == NULL)
{
// ������ʾ������Ϣ
sprintf(strbuf, " �� �� δ �� �� �� �� WinPcap �� ֧ �� %s �� ",
IfInfo[i].DeviceName);
MessageBox(strbuf);
PostMessage(WM_QUIT, 0, 0);
}
}
// �������ݰ������̣߳���ȡ���ؽӿڵ� MAC ��ַ���߳���ĿΪ��������
CWinThread* pthread;
for (i = 0; i < IfCount; i++)
{
pthread  =  AfxBeginThread(CaptureLocalARP,  &IfInfo[i],
THREAD_PRIORITY_NORMAL);
if(!pthread)
{
	MessageBox("�������ݰ������߳�ʧ�ܣ�");
	PostMessage(WM_QUIT, 0, 0);
}
}
// ���б�������Ӳ����ַ�� 0
for (i = 0; i < IfCount; i++)
{
	setMAC(IfInfo[i].MACAddr, 0);
}
// Ϊ�õ���ʵ������ַ��ʹ����ٵ� MAC ��ַ�� IP ��ַ�򱾻����� ARP ����
setMAC(srcMAC, 66); // ������ٵ� MAC ��ַ
srcIP = inet_addr("112.112.112.112"); // ������ٵ� IP ��ַ
for (i = 0; i < IfCount; i++)
{
ARPRequest(IfInfo[i].adhandle, srcMAC, srcIP, IfInfo[i].ip[0].IPAddr);
}
// ȷ�����нӿڵ� MAC ��ַ��ȫ�յ�
setMAC(srcMAC, 0);
do {
	Sleep(1000);
	k = 0;
	for (i = 0; i < IfCount; i++)
	{
		if (!cmpMAC(IfInfo[i].MACAddr, srcMAC))
		{
			k++;
			continue;
		}
		else
		{
			break;
		}
	}
} while (!((j++ > 10) || (k == IfCount)));
if (k != IfCount)
{
	char q[2];
	q[0] = k + '0';
	q[1] = '\0';
	MessageBox(q);
//	PostMessage(WM_QUIT, 0, 0);
}
// ��־����ӿ���Ϣ
for (i = 0; i < IfCount; i++)
{
LOGGER.InsertString(-1,"�ӿ�:");
LOGGER.InsertString(-1," �豸����" + IfInfo[i].DeviceName);
LOGGER.InsertString(-1," �豸������" + IfInfo[i].Description);
LOGGER.InsertString(-1,("MAC ��ַ�� "+ MACntoa(IfInfo[i].MACAddr)));
for (j = 0; j < IfInfo[i].ip.GetSize(); j++)
{
LOGGER.InsertString(-1,("IP ��ַ��"+IPntoa(IfInfo[i].ip[j].IPAddr)));
}
}
// ��ʼ��·�ɱ���ʾ
RouteTable_t rt;
for (i = 0; i < IfCount; i++)
{
for (j = 0; j < IfInfo[i].ip.GetSize(); j++)
{
rt.IfNo = i;
rt.DstIP = IfInfo[i].ip[j].IPAddr & IfInfo[i].ip[j].IPMask;
rt.Mask = IfInfo[i].ip[j].IPMask;
rt.NextHop = 0;  // ֱ��Ͷ��
RouteTable.AddTail(rt);
m_RouteTable.InsertString(-1,  IPntoa(rt.Mask)  +  "  --  "  +
IPntoa(rt.DstIP) + " -- " + IPntoa(rt.NextHop) + " (ֱ��Ͷ��)");
}
}
// ���ù��˹���:�������� arp ��Ӧ֡����Ҫ·�ɵ�֡
CString Filter, Filter0, Filter1;
Filter0 = "(";
Filter1 = "(";
for (i = 0; i < IfCount; i++)
{
Filter0 += "(ether dst " + MACntoa(IfInfo[i].MACAddr) + ")";
for (j = 0; j < IfInfo[i].ip.GetSize(); j++)
{
Filter1 += "(ip dst host " + IPntoa(IfInfo[i].ip[j].IPAddr) + ")";
if (((j == (IfInfo[i].ip.GetSize() -1))) && (i == (IfCount-1)))
{
Filter1 += ")";
}
else
{
Filter1 += " or ";
}
}
if (i == (IfCount-1)) 
{
Filter0 += ")";
}
else
{
Filter0 += " or ";
}
}
Filter = Filter0 + " and ((arp and (ether[21]=0x2)) or (not" + Filter1 + "))";
sprintf(strbuf, "%s", Filter);
//TRACE1("filter:%s /n",strbuf);
for (i = 0; i < IfCount; i++)
{
if  (pcap_compile(IfInfo[i].adhandle  ,  &fcode,  strbuf,  1,
IfInfo[i].ip[0].IPMask) <0 )
{
MessageBox("���˹�����벻�ɹ��� ������д�Ĺ����﷨�Ƿ���ȷ!");
PostMessage(WM_QUIT,0,0);
}
if (pcap_setfilter(IfInfo[i].adhandle, &fcode)<0)
{
MessageBox("���ù���������");
PostMessage(WM_QUIT, 0, 0);
}
}
// ������Ҫ���豸�б�,�ͷ�֮
pcap_freealldevs(alldevs); 
TimerCount = 1;
// ��ʼ�������ݰ�
for (i=0; i < IfCount; i++)
{
pthread  =  AfxBeginThread(Capture,  &IfInfo[i],
THREAD_PRIORITY_NORMAL);
if(!pthread)
{
MessageBox("�������ݰ������߳�ʧ�ܣ�");
PostMessage(WM_QUIT, 0, 0); 
}
}
}

void CWangluoluyouqiDlg::OnBnClickedButton() 
{
	MessageBox("666");
	// TODO: Add your control notification handler code here
// TODO: Add your message handler code here
//PostMessage(WM_QUIT,0,0);
SendMessage(WM_CLOSE);

}

void CWangluoluyouqiDlg::OnAddRouterButton() 
{
	// TODO: Add your control notification handler code here
	MessageBox("666");
	// TODO: Add your control notification handler code here
// TODO: Add your control notification handler code here
bool flag;
int i, j;
DWORD ipaddr;
RouteTable_t rt;
m_NextHop.GetAddress(ipaddr);
ipaddr = htonl(ipaddr);
// ���Ϸ���
flag = false;
for (i=0; i < IfCount; i++)
{
for (j = 0; j < IfInfo[i].ip.GetSize(); j++)
{
if  (((IfInfo[i].ip[j].IPAddr)  &  (IfInfo[i].ip[j].IPMask))  ==
((IfInfo[i].ip[j].IPMask) & ipaddr))
{
rt.IfNo = i;
// ��¼��������
m_Mask.GetAddress(ipaddr);
rt.Mask = htonl(ipaddr);
// ��¼Ŀ�� IP
m_Destination.GetAddress(ipaddr);
rt.DstIP = htonl(ipaddr);
// ��¼��һ��
m_NextHop.GetAddress(ipaddr);
rt.NextHop = htonl(ipaddr);
// �Ѹ���·�ɱ�����ӵ�·�ɱ�
RouteTable.AddTail(rt);
// ��·�ɱ�������ʾ��·�ɱ���
m_RouteTable.InsertString(-1, IPntoa(rt.Mask) + " -- "+ IPntoa(rt.DstIP) + " -- " + IPntoa(rt.NextHop));
flag = true;
}
}
}
if (!flag)
{
MessageBox("����������������룡");
}
	
}

void CWangluoluyouqiDlg::OnDeleteRouterButton() 
{
	// TODO: Add your control notification handler code here
		MessageBox("666");
		// TODO: Add your control notification handler code here
	// TODO: Add your control notification handler code here
		int i;
char str[100], ipaddr[20];
ULONG mask, destination, nexthop;
RouteTable_t rt;
POSITION pos, CurrentPos;
str[0] = NULL;
ipaddr[0] = NULL;
if ((i = m_RouteTable.GetCurSel()) == LB_ERR)
{
return;
}
m_RouteTable.GetText(i, str);
// ȡ����������ѡ��
strncat(ipaddr, str, 15);
mask = inet_addr(ipaddr);
// ȡ��Ŀ�ĵ�ַѡ��
ipaddr[0] = 0;
strncat(ipaddr, &str[19], 15);
destination = inet_addr(ipaddr);
// ȡ����һ��ѡ��
ipaddr[0] = 0;
strncat(ipaddr, &str[38], 15);
nexthop = inet_addr(ipaddr);
if (nexthop == 0)
{
MessageBox("ֱ������·�ɣ�������ɾ����");
return;
}
// �Ѹ�·�ɱ����·�ɱ�����ɾ��
m_RouteTable.DeleteString(i);
// ·�ɱ���û����Ҫ��������ݣ��򷵻�
if (RouteTable.IsEmpty())
{
return;
}
// ����·�ɱ�,����Ҫɾ����·�ɱ����·�ɱ���ɾ��
pos = RouteTable.GetHeadPosition();
for (i=0; i<RouteTable.GetCount(); i++)
{ 
CurrentPos = pos;
rt = RouteTable.GetNext(pos);
if ((rt.Mask == mask) && (rt.DstIP == destination) && (rt.NextHop ==
nexthop))
{
RouteTable.RemoveAt(CurrentPos);
return;
}
}

}
// ��ȡ���ؽӿ� MAC ��ַ�߳�
UINT CaptureLocalARP(PVOID pParam)
{
int res;
struct pcap_pkthdr *header;
const u_char *pkt_data;
IfInfo_t *pIfInfo;
ARPFrame_t *ARPFrame;
CString DisplayStr;
pIfInfo = (IfInfo_t *)pParam;
AfxMessageBox("777");
while (true)
{
Sleep(50);
res = pcap_next_ex( pIfInfo->adhandle , &header, &pkt_data);
// ��ʱ
if (res == 0)
continue;
if (res > 0)
{
ARPFrame = (ARPFrame_t *) (pkt_data);
// �õ����ӿڵ� MAC ��ַ
if ((ARPFrame->FrameHeader.FrameType == htons(0x0806))
&& (ARPFrame->Operation == htons(0x0002))
&& (ARPFrame->SendIP == pIfInfo->ip[0].IPAddr))
{
cpyMAC(pIfInfo->MACAddr, ARPFrame->SendHa);
return 0;
}
}
}
}
void setMAC(UCHAR *MAC, UCHAR ch)
{
for (int i=0; i<6; i++)
{
MAC[i] = ch;
}
return;
}
// ���� ARP ����
void ARPRequest(pcap_t *adhandle, UCHAR *srcMAC, ULONG srcIP, ULONG
targetIP)
{
ARPFrame_t ARPFrame;
int i;
for (i=0; i<6; i++)
{
ARPFrame.FrameHeader.DesMAC[i] = 255;
ARPFrame.FrameHeader.SrcMAC[i] = srcMAC[i];
ARPFrame.SendHa[i] = srcMAC[i];
ARPFrame.RecvHa[i] = 0;
}
ARPFrame.FrameHeader.FrameType = htons(0x0806);
ARPFrame.HardwareType = htons(0x0001);
ARPFrame.ProtocolType = htons(0x0800);
ARPFrame.HLen = 6;
ARPFrame.PLen = 4;
ARPFrame.Operation = htons(0x0001);
ARPFrame.SendIP = srcIP;
ARPFrame.RecvIP = targetIP;
pcap_sendpacket(adhandle, (u_char *) &ARPFrame, sizeof(ARPFrame_t));
}
void cpyMAC(UCHAR *MAC1, UCHAR *MAC2)
{
for (int i=0; i<6; i++)
{
MAC1[i]=MAC2[i];
}
}
// �Ƚ����� MAC ��ַ�Ƿ���ͬ
bool cmpMAC(UCHAR *MAC1, UCHAR *MAC2)
{
for (int i=0; i<6; i++)
{
if (MAC1[i]==MAC2[i])
{
continue;
}
else
{
return false;
}
}
return true;
}
// �� IP ��ַת���ɵ��ʮ������ʽ
CString IPntoa(ULONG nIPAddr)
{
char strbuf[50];
u_char  *p;
CString str;
p = (u_char *) &nIPAddr;
sprintf(strbuf,"%03d.%03d.%03d.%03d", p[0], p[1], p[2], p[3]);
str = strbuf;
return str;
}
// �� MAC ��ַת���ɡ�%02X:%02X:%02X:%02X:%02X:%02X���ĸ�ʽ
CString MACntoa(UCHAR *nMACAddr)
{
char strbuf[50];
CString str;
sprintf(strbuf,"%02X:%02X:%02X:%02X:%02X:%02X",  nMACAddr[0],
nMACAddr[1],
nMACAddr[2], nMACAddr[3], nMACAddr[4], nMACAddr[5]);
str = strbuf;
return str;
}
// ���ݰ������߳�
UINT Capture(PVOID pParam)
{
int res;
IfInfo_t *pIfInfo;
struct pcap_pkthdr *header;
const u_char *pkt_data;
pIfInfo = (IfInfo_t *)pParam;
// ��ʼ��ʽ���ղ�����֡
while (true) 
{
res = pcap_next_ex( pIfInfo->adhandle, &header, &pkt_data);
if (res == 1)
{
FrameHeader_t  *fh;
fh = (FrameHeader_t *) pkt_data;
switch (ntohs(fh->FrameType))
{
case 0x0806:
	ARPFrame_t *ARPf;
ARPf = (ARPFrame_t *)pkt_data;
//TRACE1(" �� �� ARP �� Դ IP Ϊ �� %d\n",ARPf->SendIP);
// ARP ����ת�� ARP ��������
ARPPacketProc(header, pkt_data);
break;
case 0x0800:
IPFrame_t *IPf;
IPf = (IPFrame_t*) pkt_data;
//TRACE1(" �� �� IP �� Դ IPΪ��%d\n",IPf->IPHeader.SrcIP );
// IP ����ת�� IP ��������
IPPacketProc(pIfInfo, header, pkt_data);
break;
default:
break;
}
}
else if (res == 0)  // ��ʱ
{
continue;
}
else
{
AfxMessageBox("pcap_next_ex ��������!");
}
}
return 0;
}
// ���� ARP ���ݰ�
void ARPPacketProc(struct pcap_pkthdr *header, const u_char *pkt_data)
{
bool flag;
ARPFrame_t ARPf;
IPFrame_t  *IPf;
SendPacket_t sPacket;
POSITION  pos, CurrentPos;
IP_MAC_t  ip_mac;
UCHAR macAddr[6];
ARPf = *(ARPFrame_t *)pkt_data;
if (ARPf.Operation == ntohs(0x0002))
{
pDlg->LOGGER.InsertString(-1, "�յ� ARP ��Ӧ��");
pDlg->LOGGER.InsertString(-1, (" ARP "+(IPntoa(ARPf.SendIP))+" -- "
+MACntoa(ARPf.SendHa)));
// IP��MAC ��ַӳ������Ѿ����ڸö�Ӧ��ϵ
if (IPLookup(ARPf.SendIP, macAddr))
{
pDlg->LOGGER.InsertString(-1, " �ö�Ӧ��ϵ�Ѿ������� IP��MAC ��ַӳ�����");
return; 
} 
else
{
ip_mac.IPAddr = ARPf.SendIP; 
memcpy(ip_mac.MACAddr, ARPf.SendHa, 6);
// �� IP-MAC ӳ���ϵ������� 
IP_MAC.AddHead(ip_mac);
// ��־�����Ϣ
pDlg->LOGGER.InsertString(-1, " ���ö�Ӧ��ϵ���� IP��MAC��ַӳ�����");
}
mMutex.Lock(INFINITE);
do{
	// �鿴�Ƿ���ת�������е� IP ���ݱ�
flag = false;
// û����Ҫ���������
if (SP.IsEmpty())
{
break; 
}
// ����ת��������
pos = SP.GetHeadPosition();
for (int i=0; i < SP.GetCount(); i++)
{ 
CurrentPos = pos;
sPacket = SP.GetNext(pos);
if (sPacket.TargetIP == ARPf.SendIP)
{
IPf = (IPFrame_t *) sPacket.PktData;
cpyMAC(IPf->FrameHeader.DesMAC, ARPf.SendHa);
for(int t=0; t<6; t++)
{
IPf->FrameHeader.SrcMAC[t]  =
IfInfo[sPacket.IfNo].MACAddr[t];
}
// ���� IP ���ݰ�
pcap_sendpacket(IfInfo[sPacket.IfNo].adhandle,  (u_char
*) sPacket.PktData, sPacket.len);
SP.RemoveAt(CurrentPos);
// ��־�����Ϣ
pDlg->LOGGER.InsertString(-1, " ת����������Ŀ�ĵ�ַ�Ǹ� MAC ��ַ�� IP ���ݰ�");
pDlg->LOGGER.InsertString(-1, (" ���� IP ���ݰ���"+IPntoa(IPf->IPHeader.SrcIP) + "->"+  IPntoa(IPf->IPHeader.DstIP)  +  "  "  +MACntoa(IPf->FrameHeader.SrcMAC )+"->"+MACntoa(IPf->FrameHeader.DesMAC)));
flag = true;
break;
}
}
} while(flag);
mMutex.Unlock();
}
}
// ��ѯ IP-MAC ӳ���
bool IPLookup(ULONG ipaddr, UCHAR *p)
{
IP_MAC_t  ip_mac;
POSITION  pos;
if (IP_MAC.IsEmpty()) return false;
pos = IP_MAC.GetHeadPosition();
for (int i = 0; i<IP_MAC.GetCount(); i++)
{
ip_mac = IP_MAC.GetNext(pos);
if (ipaddr == ip_mac.IPAddr)
{
for (int j = 0; j < 6; j++)
{
p[j] = ip_mac.MACAddr[j];
}
return true;
}
}
return false;
}
// ���� IP ���ݰ�
void IPPacketProc(IfInfo_t *pIfInfo, struct pcap_pkthdr *header, const u_char
*pkt_data)
{
IPFrame_t  *IPf;
SendPacket_t sPacket;
IPf = (IPFrame_t *) pkt_data;
pDlg->LOGGER.InsertString(-1,  (" �� �� IP �� �� �� :"  +
IPntoa(IPf->IPHeader.SrcIP) + "->"
+ IPntoa(IPf->IPHeader.DstIP)));
// ICMP ��ʱ
if (IPf->IPHeader.TTL <= 0)
{
ICMPPacketProc(pIfInfo, 11, 0, pkt_data);
return;
}
IPHeader_t *IpHeader = &(IPf->IPHeader);
// ICMP ���
if (IsChecksumRight((char *)IpHeader) == 0)
{
// ��־�����Ϣ
pDlg->LOGGER.InsertString(-1, " IP ���ݰ���ͷУ��ʹ��󣬶������ݰ�");
return;
}
// ·�ɲ�ѯ
DWORD nextHop; // ����·��ѡ���㷨�õ�����һվĿ�� IP ��ַ
UINT ifNo;  // ��һ���Ľӿ����
// ·�ɲ�ѯ
if((nextHop = RouteLookup(ifNo, IPf->IPHeader.DstIP, &RouteTable)) == -1)
{
	// ICMP Ŀ�Ĳ��ɴ�
ICMPPacketProc(pIfInfo, 3, 0, pkt_data);
return;
}
else
{
sPacket.IfNo = ifNo;
sPacket.TargetIP = nextHop;
cpyMAC(IPf->FrameHeader.SrcMAC, IfInfo[sPacket.IfNo].MACAddr);
// TTL �� 1
IPf->IPHeader.TTL -= 1;
unsigned short check_buff[sizeof(IPHeader_t)];
// �� IP ͷ�е�У���Ϊ 0
IPf->IPHeader.Checksum = 0;
memset(check_buff, 0, sizeof(IPHeader_t));
IPHeader_t * ip_header = &(IPf->IPHeader);
memcpy(check_buff, ip_header, sizeof(IPHeader_t));
// ���� IP ͷ��У���
IPf->IPHeader.Checksum  =  ChecksumCompute(check_buff,
sizeof(IPHeader_t));
// IP-MAC ��ַӳ����д��ڸ�ӳ���ϵ
if (IPLookup(sPacket.TargetIP, IPf->FrameHeader.DesMAC))
{
memcpy(sPacket.PktData, pkt_data, header->len);
sPacket.len = header->len;
if(pcap_sendpacket(IfInfo[sPacket.IfNo].adhandle,  (u_char  *)
sPacket.PktData, sPacket.len) != 0)
{
// ������
AfxMessageBox("���� IP ���ݰ�ʱ����!");
return;
}
// ��־�����Ϣ
pDlg->LOGGER.InsertString(-1," ת�� IP ���ݰ���");
pDlg->LOGGER.InsertString(-1,(" "+IPntoa(IPf->IPHeader.SrcIP)
+ "->"
+  IPntoa(IPf->IPHeader.DstIP)  +  "  "  +
MACntoa(IPf->FrameHeader.SrcMAC )
+ "->" + MACntoa(IPf->FrameHeader.DesMAC)));
}
// IP-MAC ��ַӳ����в����ڸ�ӳ���ϵ
else
{
if (SP.GetCount() < 65530)  // ���뻺�����
{
sPacket.len = header->len; 
// ����Ҫת�������ݱ����뻺����
memcpy(sPacket.PktData, pkt_data, header->len);
// ��ĳһʱ��ֻ����һ���߳�ά������
mMutex.Lock(INFINITE); 
sPacket.n_mTimer = TimerCount;
if (TimerCount++ > 65533)
{
TimerCount = 1;
}
pDlg->SetTimer(sPacket.n_mTimer, 10000, NULL);
SP.AddTail(sPacket);
mMutex.Unlock();
// ��־�����Ϣ
pDlg->LOGGER.InsertString(-1, " ȱ��Ŀ�� MAC ��ַ�� �� IP���ݰ�����ת��������");
pDlg->LOGGER.InsertString(-1, (" ����ת�������������ݰ�Ϊ��"+IPntoa(IPf->IPHeader.SrcIP)+ "->" + IPntoa(IPf->IPHeader.DstIP) + " " +MACntoa(IPf->FrameHeader.SrcMAC)+ "->xx:xx:xx:xx:xx:xx"));
pDlg->LOGGER.InsertString(-1, " ���� ARP ����");
// ���� ARP ����
ARPRequest(IfInfo[sPacket.IfNo].adhandle,
IfInfo[sPacket.IfNo].MACAddr,
IfInfo[sPacket.IfNo].ip[1].IPAddr, sPacket.TargetIP);
} 
else // �绺�����̫���������ñ�
{
// ��־�����Ϣ
pDlg->LOGGER.InsertString(-1, " ת����������������� IP���ݰ�");
pDlg->LOGGER.InsertString(-1, (" ������ IP ���ݰ�Ϊ��" +IPntoa(IPf->IPHeader.SrcIP) + "->"+  IPntoa(IPf->IPHeader.DstIP)  +  "  "  +MACntoa(IPf->FrameHeader.SrcMAC)+ "->xx:xx:xx:xx:xx:xx"));
}
} 
}
}
// �ж� IP ���ݰ�ͷ��У����Ƿ���ȷ
int IsChecksumRight(char * buffer)
{
// ��� IP ͷ����
IPHeader_t * ip_header = (IPHeader_t *)buffer;
// ����ԭ����У���
unsigned short checksumBuf = ip_header->Checksum;
unsigned short check_buff[sizeof(IPHeader_t)];
// �� IP ͷ�е�У���Ϊ 0
ip_header->Checksum = 0;
memset(check_buff, 0, sizeof(IPHeader_t));
memcpy(check_buff, ip_header, sizeof(IPHeader_t));
// ���� IP ͷ��У���
ip_header->Checksum = ChecksumCompute(check_buff, sizeof(IPHeader_t));
// �뱸�ݵ�У��ͽ��бȽ�
if (ip_header->Checksum == checksumBuf)
{
return 1;
}
else
{
return 0;
}
}
// ��ѯ·�ɱ�
DWORD RouteLookup(UINT &ifNO, DWORD desIP, CList <RouteTable_t,
RouteTable_t&> *routeTable)
{
// desIP Ϊ������
DWORD MaxMask = 0; // ���������������ĵ�ַ�� û�л��ʱ��ʼ��Ϊ-1
int Index = -1;  // ���������������ĵ�ַ��Ӧ��·�ɱ��������Ա�����һվ·�����ĵ�ַ
POSITION  pos;
RouteTable_t rt;
DWORD tmp;
pos = routeTable->GetHeadPosition();
for (int i=0; i < routeTable->GetCount(); i++)
{
rt = routeTable->GetNext(pos);
if ((desIP & rt.Mask) == rt.DstIP)
{
	Index = i;
if(rt.Mask >= MaxMask)
{
ifNO = rt.IfNo;
if (rt.NextHop == 0)  // ֱ��Ͷ��
{
tmp = desIP;
}
else
{ 
tmp = rt.NextHop;
}
} 
}
}
if(Index == -1)  // Ŀ�Ĳ��ɴ�
{
return -1;
}
else // �ҵ�����һ����ַ
{
return tmp;
} 
}
// ���� ICMP ���ݰ�
void ICMPPacketProc(IfInfo_t *pIfInfo, BYTE type, BYTE code, const u_char
*pkt_data)
{
u_char * ICMPBuf = new u_char[70];
// ���֡�ײ�
memcpy(((FrameHeader_t  *)ICMPBuf)->DesMAC,  ((FrameHeader_t
	   *)pkt_data)->SrcMAC, 6);
memcpy(((FrameHeader_t  *)ICMPBuf)->SrcMAC,  ((FrameHeader_t
*)pkt_data)->DesMAC, 6);
((FrameHeader_t *)ICMPBuf)->FrameType = htons(0x0800);
// ��� IP �ײ�
((IPHeader_t  *)(ICMPBuf+14))->Ver_HLen  =  ((IPHeader_t
*)(pkt_data+14))->Ver_HLen;
((IPHeader_t  *)(ICMPBuf+14))->TOS  =  ((IPHeader_t
*)(pkt_data+14))->TOS;
((IPHeader_t *)(ICMPBuf+14))->TotalLen = htons(56);
((IPHeader_t *)(ICMPBuf+14))->ID = ((IPHeader_t *)(pkt_data+14))->ID;
((IPHeader_t  *)(ICMPBuf+14))->Flag_Segment  =  ((IPHeader_t
*)(pkt_data+14))->Flag_Segment;
((IPHeader_t *)(ICMPBuf+14))->TTL = 64;
((IPHeader_t *)(ICMPBuf+14))->Protocol = 1;
((IPHeader_t  *)(ICMPBuf+14))->SrcIP  =  ((IPHeader_t
*)(pkt_data+14))->DstIP;
((IPHeader_t  *)(ICMPBuf+14))->DstIP  =  ((IPHeader_t
*)(pkt_data+14))->SrcIP;
((IPHeader_t  *)(ICMPBuf+14))->Checksum  =
htons(ChecksumCompute((unsigned short *)(ICMPBuf+14),20));
// ��� ICMP �ײ�
((ICMPHeader_t *)(ICMPBuf+34))->Type = type;
((ICMPHeader_t *)(ICMPBuf+34))->Code = code;
((ICMPHeader_t *)(ICMPBuf+34))->Id = 0;
((ICMPHeader_t *)(ICMPBuf+34))->Sequence = 0;
((ICMPHeader_t  *)(ICMPBuf+34))->Checksum  =
htons(ChecksumCompute((unsigned short *)(ICMPBuf+34),8));
// �������
memcpy((u_char *)(ICMPBuf+42),(IPHeader_t *)(pkt_data+14),20);
memcpy((u_char *)(ICMPBuf+62),(u_char *)(pkt_data+34),8);
// �������ݰ�
pcap_sendpacket(pIfInfo->adhandle, (u_char *)ICMPBuf, 70 );
// ��־�����Ϣ
if (type == 11)
{
pDlg->LOGGER.InsertString(-1, " ���� ICMP ��ʱ���ݰ���");
}
if (type == 3)
{
pDlg->LOGGER.InsertString(-1, " ���� ICMP Ŀ�Ĳ��ɴ����ݰ���");
}
pDlg->LOGGER.InsertString(-1, (" ICMP ->" + IPntoa(((IPHeader_t
*)(ICMPBuf+14))->DstIP)
+ "-" + MACntoa(((FrameHeader_t *)ICMPBuf)->DesMAC)));
delete [] ICMPBuf;
}
// ����У���
unsigned short ChecksumCompute(unsigned short * buffer,int size)
{
// 32 λ���ӳٽ�λ
unsigned long cksum = 0;
while (size > 1)
{
cksum += * buffer++;
// 16 λ���
size -= sizeof(unsigned short);
}
if(size)
{
// �������е��� 8 λ
cksum += *(unsigned char *)buffer;
}
// ���� 16 λ��λ������ 16 λ
cksum = (cksum >> 16) + (cksum & 0xffff);
cksum += (cksum >> 16);
// ȡ��
return (unsigned short)(~cksum);
}

void CWangluoluyouqiDlg::OnDestroy() 
{
	CDialog::OnDestroy();
	SP.RemoveAll();
	IP_MAC.RemoveAll();
	RouteTable.RemoveAll();
	for (int i=0; i<IfCount; i++)
	{
		IfInfo[i].ip.RemoveAll();
	}
		
}

void CWangluoluyouqiDlg::OnTimer(UINT nIDEvent) 
{
	SendPacket_t sPacket;
POSITION pos, CurrentPos;
IPFrame_t *IPf;
// û����Ҫ���������
if (SP.IsEmpty())
{
return; 
}
mMutex.Lock(INFINITE);
// ����ת��������
pos = SP.GetHeadPosition();
for (int i = 0; i < SP.GetCount(); i++)
{ 
CurrentPos = pos;
sPacket = SP.GetNext(pos);
if (sPacket.n_mTimer == nIDEvent)
{
IPf = (IPFrame_t *)sPacket.PktData;
// ��־�����Ϣ
LOGGER.InsertString(-1, "IP ���ݱ���ת�������еȴ� 10 ���δ�ܱ�ת��");
LOGGER.InsertString(-1, (" ��ʱ����ɾ���� IP ���ݱ���" +IPntoa(IPf->IPHeader.SrcIP) + "->"+  IPntoa(IPf->IPHeader.DstIP)  +  "  "  +MACntoa(IPf->FrameHeader.SrcMAC)+ "->xx:xx:xx:xx:xx:xx"));
KillTimer(sPacket.n_mTimer);
SP.RemoveAt(CurrentPos);
}
}
mMutex.Unlock();
	CDialog::OnTimer(nIDEvent);
}
