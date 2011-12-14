;The name of the installer
Name "On Screen Server Installer"

;The file to write
OutFile "OnScreenServer.exe"

; Show install details
ShowInstDetails show

InstallDir $PROGRAMFILES\OnScreen

Section "Installer"
	SetOutPath $INSTDIR
	File /r C:\Users\Mattias\Documents\NetBeansProjects\OnScreenServer\dist\*.jar
	File C:\Users\Mattias\Documents\NetBeansProjects\OnScreenServer\SumatraPDF.exe
	File C:\Users\Mattias\Documents\NetBeansProjects\OnScreenServer\tray.gif
	WriteUninstaller "$INSTDIR\Uninstall.exe"
	CreateShortCut "$SMPROGRAMS\Startup\OnScreen.lnk" "$INSTDIR\OnScreen.jar"
    ExecShell "" "$SMPROGRAMS\Startup\OnScreen.lnk"
SectionEnd

Section "Uninstall"
  Delete "$INSTDIR\Uninstall.exe"
  RMDir /r "$INSTDIR\lib"
  RMDir /r "$INSTDIR"
  Delete "$SMPROGRAMS\Startup\OnScreen.lnk"
SectionEnd
