@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Maven Start Up Batch script
@REM ----------------------------------------------------------------------------

@IF "%DEBUG%"=="" @ECHO OFF
@REM set %ENABLE_DELAYED_EXPANSION% to on if you want to use !variable! syntax
SETLOCAL ENABLEDELAYEDEXPANSION ENABLEEXTENSIONS

IF NOT "%MAVEN_SKIP_RC%"=="" GOTO skipRcPre
IF EXIST "%USERPROFILE%\.mavenrc.cmd" CALL "%USERPROFILE%\.mavenrc.cmd" >NUL 2>&1
:skipRcPre

@REM To check if Java is installed
IF NOT "%JAVA_HOME%"=="" GOTO checkJava
FOR %%i IN (java.exe) DO SET JAVA_EXE=%%~$PATH:i
IF NOT "%JAVA_EXE%"=="" GOTO findJavaInPath

ECHO Error: JAVA_HOME not found in your environment.
ECHO Please set the JAVA_HOME variable in your environment to match the
ECHO location of your Java installation.
GOTO error

:findJavaInPath
SET JAVA_EXE="%JAVA_EXE%"
GOTO runMvn

:checkJava
IF EXIST "%JAVA_HOME%\bin\java.exe" (
  SET JAVA_EXE="%JAVA_HOME%\bin\java.exe"
  GOTO runMvn
)
ECHO Error: JAVA_HOME is set to an invalid directory: "%JAVA_HOME%"
ECHO Please set the JAVA_HOME variable in your environment to match the
ECHO location of your Java installation.
GOTO error

:runMvn
SET MAVEN_PROJECTBASEDIR=%~dp0
IF "%MAVEN_PROJECTBASEDIR:~-1%"=="\" SET MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%

SET WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
SET WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

IF EXIST %WRAPPER_JAR% GOTO runWrapper

@REM Download wrapper jar if not present
powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object Net.WebClient).DownloadFile('https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar', '%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar')}" >NUL 2>&1

:runWrapper
%JAVA_EXE% %JVM_CONFIG_MAVEN_PROPS% -classpath %WRAPPER_JAR% "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" %WRAPPER_LAUNCHER% %*
IF ERRORLEVEL 1 GOTO error
GOTO end

:error
EXIT /B 1

:end
EXIT /B 0
