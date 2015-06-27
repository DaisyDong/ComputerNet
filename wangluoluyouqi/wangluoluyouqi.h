// wangluoluyouqi.h : main header file for the WANGLUOLUYOUQI application
//

#if !defined(AFX_WANGLUOLUYOUQI_H__4F0584B7_6AD3_46B6_AED2_CA4F3FDADC10__INCLUDED_)
#define AFX_WANGLUOLUYOUQI_H__4F0584B7_6AD3_46B6_AED2_CA4F3FDADC10__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#ifndef __AFXWIN_H__
	#error include 'stdafx.h' before including this file for PCH
#endif

#include "resource.h"		// main symbols

/////////////////////////////////////////////////////////////////////////////
// CWangluoluyouqiApp:
// See wangluoluyouqi.cpp for the implementation of this class
//

class CWangluoluyouqiApp : public CWinApp
{
public:
	CWangluoluyouqiApp();

// Overrides
	// ClassWizard generated virtual function overrides
	//{{AFX_VIRTUAL(CWangluoluyouqiApp)
	public:
	virtual BOOL InitInstance();
	//}}AFX_VIRTUAL

// Implementation

	//{{AFX_MSG(CWangluoluyouqiApp)
		// NOTE - the ClassWizard will add and remove member functions here.
		//    DO NOT EDIT what you see in these blocks of generated code !
	//}}AFX_MSG
	DECLARE_MESSAGE_MAP()
};


/////////////////////////////////////////////////////////////////////////////

//{{AFX_INSERT_LOCATION}}
// Microsoft Visual C++ will insert additional declarations immediately before the previous line.

#endif // !defined(AFX_WANGLUOLUYOUQI_H__4F0584B7_6AD3_46B6_AED2_CA4F3FDADC10__INCLUDED_)
