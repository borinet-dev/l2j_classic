@echo off
COLOR 0b
title 보리넷 게임서버


:start
echo 게임서버 시작
echo.
if not exist log (mkdir log)
echo > log/stdout.log

for /f "delims=" %%x in (java.cfg) do set JVM_CFG=%%x
java %JVM_CFG% -XX:+UnlockExperimentalVMOptions -XX:+UseZGC -cp ../libs/smrt.jar;../libs/smrt-core-1.0.jar;../libs/* smartguard.SmartGuard org.l2jmobius.gameserver.GameServer > log/stdout.log 2>&1

if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end
:restart
echo Server restarting
goto start
ping -n 60 localhost > NUL
:error
echo Server terminated abnormaly
:end
echo Server terminated
pause
