// wangluoluyouqiDlg.h : header file
//

#if !defined(AFX_WANGLUOLUYOUQIDLG_H__88ECB50A_7669_407E_AF65_40E7CE06AAB3__INCLUDED_)
#define AFX_WANGLUOLUYOUQIDLG_H__88ECB50A_7669_407E_AF65_40E7CE06AAB3__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

/////////////////////////////////////////////////////////////////////////////
// CWangluoluyouqiDlg dialog

class CWangluoluyouqiDlg : public CDialog
{
// Construction
public:
	CWangluoluyouqiDlg(CWnd* pParent = NULL);	// standard constructor

// Dialog Data
	//{{AFX_DATA(CWangluoluyouqiDlg)
	enum { IDD = IDD_WANGLUOLUYOUQI_DIALOG };
	CIPAddressCtrl	m_Destination;
	CIPAddressCtrl	m_NextHop;
	CIPAddressCtrl	m_Mask;
	CListBox	m_RouteTable;
	CListBox	LOGGER;
	//}}AFX_DATA

	// ClassWizard generated virtual function overrides
	//{{AFX_VIRTUAL(CWangluoluyouqiDlg)
	protected:
	virtual void DoDataExchange(CDataExchange* pDX);	// DDX/DDV support
	//}}AFX_VIRTUAL

// Implementation
protected:
	HICON m_hIcon;

	// Generated message map functions
	//{{AFX_MSG(CWangluoluyouqiDlg)
	virtual BOOL OnInitDialog();
	afx_msg void OnSysCommand(UINT nID, LPARAM lParam);
	afx_msg void OnPaint();
	afx_msg HCURSOR OnQueryDragIcon();
	afx_msg void OnStartClickedButton();
	afx_msg void OnBnClickedButton();
	afx_msg void OnAddRouterButton();
	afx_msg void OnDeleteRouterButton();
	afx_msg void OnDestroy();
	afx_msg void OnTimer(UINT nIDEvent);
	//}}AFX_MSG
	DECLARE_MESSAGE_MAP()
};

//{{AFX_INSERT_LOCATION}}
// Microsoft Visual C++ will insert additional declarations immediately before the previous line.

#endif // !defined(AFX_WANGLUOLUYOUQIDLG_H__88ECB50A_7669_407E_AF65_40E7CE06AAB3__INCLUDED_)
