#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Start Up Shell Script
# ----------------------------------------------------------------------------

MAVEN_PROJECTBASEDIR="${0%/*}"
WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_LAUNCHER="org.apache.maven.wrapper.MavenWrapperMain"

if [ -z "$JAVA_HOME" ] ; then
  JAVACMD="java"
else
  JAVACMD="$JAVA_HOME/bin/java"
fi

if [ ! -f "$WRAPPER_JAR" ]; then
  mkdir -p "$MAVEN_PROJECTBASEDIR/.mvn/wrapper"
  curl -fsSL -o "$WRAPPER_JAR" https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar
fi

exec "$JAVACMD" -classpath "$WRAPPER_JAR" "-Dmaven.multiModuleProjectDirectory=$MAVEN_PROJECTBASEDIR" $WRAPPER_LAUNCHER "$@"
