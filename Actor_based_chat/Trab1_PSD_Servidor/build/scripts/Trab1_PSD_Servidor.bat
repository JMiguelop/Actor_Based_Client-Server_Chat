@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  Trab1_PSD_Servidor startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

@rem Add default JVM options here. You can also use JAVA_OPTS and TRAB_PSD_SERVIDOR_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto init

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto init

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:init
@rem Get command-line arguments, handling Windowz variants

if not "%OS%" == "Windows_NT" goto win9xME_args
if "%@eval[2+2]" == "4" goto 4NT_args

:win9xME_args
@rem Slurp the command line arguments.
set CMD_LINE_ARGS=
set _SKIP=2

:win9xME_args_slurp
if "x%~1" == "x" goto execute

set CMD_LINE_ARGS=%*
goto execute

:4NT_args
@rem Get arguments from the 4NT Shell from JP Software
set CMD_LINE_ARGS=%$

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\Trab1_PSD_Servidor.jar;%APP_HOME%\lib\jzmq-3.1.0.jar;%APP_HOME%\lib\quasar-core-0.7.3-jdk8.jar;%APP_HOME%\lib\quasar-actors-0.7.3.jar;%APP_HOME%\lib\metrics-core-3.1.2.jar;%APP_HOME%\lib\LatencyUtils-2.0.2.jar;%APP_HOME%\lib\kryo-serializers-0.36.jar;%APP_HOME%\lib\HdrHistogram-2.1.3.jar;%APP_HOME%\lib\disruptor-3.3.2.jar;%APP_HOME%\lib\guava-18.0.jar;%APP_HOME%\lib\kryo-2.24.0.jar;%APP_HOME%\lib\byte-buddy-0.6.15.jar;%APP_HOME%\lib\protobuf-java-2.6.1.jar;%APP_HOME%\lib\minlog-1.2.jar;%APP_HOME%\lib\objenesis-2.1.jar;%APP_HOME%\lib\slf4j-api-1.7.12.jar

@rem Execute Trab1_PSD_Servidor
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %TRAB_PSD_SERVIDOR_OPTS%  -classpath "%CLASSPATH%" chatActors.ActorChatServer %CMD_LINE_ARGS%

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable TRAB_PSD_SERVIDOR_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%TRAB_PSD_SERVIDOR_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
