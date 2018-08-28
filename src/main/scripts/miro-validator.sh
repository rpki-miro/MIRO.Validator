#!/usr/bin/env bash

EXECUTION_DIR=`dirname "$BASH_SOURCE"`
cd ${EXECUTION_DIR}

APP_NAME="miro-validator"

function error_exit {
    echo -e "[ error ] $1"
    exit 1
}

function info {
    echo -e "[ info ] $1"
}

function warn {
    echo -e "[ warn ] $1"
}

function usage {
cat << EOF
Usage: $0 /path/to/my-configuration.conf
EOF
}


function check_java_version {
  JAVA_VERSION=`${JAVA_CMD} -version 2>&1 | grep version | sed 's/.* version //g'`
  MAJOR_VERSION=`echo ${JAVA_VERSION} | sed 's/"\([[:digit:]]\)\.\([[:digit:]]\).*"/\1\2/g'`
  if (( ${MAJOR_VERSION} < 18 )) ; then
    error_exit "MIRO Validator requires Java 8 or greater, your version of java is ${JAVA_VERSION}";
  fi
}


CONF_FILE=$1
#
# Specify the location of the Java home directory. If set then $JAVA_CMD will
# be defined to $JAVA_HOME/bin/java
#
if [ -d "${JAVA_HOME}"  ] ; then
    JAVA_CMD="${JAVA_HOME}/bin/java"
else
    warn "JAVA_HOME is not set, will try to find java on path."
    JAVA_CMD=`which java`
fi

if [ -z $JAVA_CMD ]; then
    error_exit "Cannot find java on path. Make sure java is installed and/or set JAVA_HOME"
fi

check_java_version

if [[ -n $MODE ]]; then
   #usage
   exit
fi

if [ ! -f ${CONF_FILE} ]; then
    echo "File ${CONF_FILE} not found!"
fi

JAR_NAME="MIRO.Validator-0.0.2-SNAPSHOT-jar-with-dependencies.jar"
CMDLINE="${JAVA_CMD} -jar ${JAR_NAME} ${CONF_FILE}"
${CMDLINE}
exit $?
