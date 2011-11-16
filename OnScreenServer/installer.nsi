;InstallOptions Test Script
;Written by Joost Verburg
;--------------------------

;The name of the installer
Name "On Screen Server Installer"

;The file to write
OutFile "OnScreenServer.exe"

; Show install details
ShowInstDetails show

InstallDir $PROGRAMFILES\OnScreen

Section "Installer"
	SetOutPath $INSTDIR
	File /r C:\Users\Mattias\Documents\NetBeansProjects\OnScreen\dist\*.jar
	File C:\Users\Mattias\Documents\NetBeansProjects\OnScreen\SumatraPDF.exe

	CreateShortCut "$SMPROGRAMS\Startup\OnScreen.lnk" "$INSTDIR\OnScreen.jar"	
SectionEnd
