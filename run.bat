@echo off
title Modern Java Browser Platform
echo Starting FastAPI AI Chatbot Service...
start /b "" python ai_service.py
timeout /t 1 >nul
echo Starting Modern Java Browser...
java -Dcom.sun.webkit.useHTTP2Loader=false -Djava.net.preferIPv4Stack=true -jar "%~dp0target\modern-browser-2.0.0.jar"
