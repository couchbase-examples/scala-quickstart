FROM couchbase:latest

RUN echo "* soft nproc 20000\n"\
"* hard nproc 20000\n"\
"* soft nofile 200000\n"\
"* hard nofile 200000\n" >> /etc/security/limits.conf

RUN apt-get -qq update && \
    apt-get install -yq default-jdk git sudo

COPY startcb.sh /opt/couchbase/bin/startcb.sh

# Install Scala and SBT
ARG SCALA_VERSION
ENV SCALA_VERSION ${SCALA_VERSION:-2.13.8}
ARG SBT_VERSION
ENV SBT_VERSION ${SBT_VERSION:-1.6.1}


# Adapted from https://github.com/hseeberger/scala-sbt/blob/master/debian/Dockerfile and
# https://github.com/broadinstitute/scala-baseimage
ARG SBT_VERSION
RUN \
  curl -L "https://github.com/sbt/sbt/releases/download/v$SBT_VERSION/sbt-$SBT_VERSION.tgz" | tar zxf - -C /usr/share  && \
  cd /usr/share/sbt/bin && \
  rm sbt.bat sbtn-x86_64-apple-darwin sbtn-x86_64-pc-linux sbtn-x86_64-pc-win32.exe && \
  ln -s /usr/share/sbt/bin/sbt /usr/local/bin/sbt

# Warm up SBT cache
ARG SCALA_VERSION
RUN \
  mkdir /setup-project && \
  cd /setup-project && \
  echo "scalaVersion := \"${SCALA_VERSION}\"" > build.sbt && \
  echo "case object Temp" > Temp.scala && \
  sbt compile && \
  rm -rf /setup-project

RUN addgroup --gid 33333 gitpod && \
     useradd --no-log-init --create-home --home-dir /home/gitpod --shell /bin/bash --uid 33333 --gid 33333 gitpod && \
     usermod -a -G gitpod,couchbase,sudo gitpod && \
     echo 'gitpod ALL=(ALL) NOPASSWD:ALL'>> /etc/sudoers

USER gitpod