@echo off

"%~dp0\launcher.bat" -main adams.gui.Main -memory 1g -title Happy-ADAMS -env-modifier adams.core.management.WekaHomeEnvironmentModifier
