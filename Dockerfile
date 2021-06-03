FROM ubuntu:18.04

# base tools
RUN apt-get update && \
    apt-get install -y \
    vim \
    git \
    wget \
    unzip \
    curl \
    gnupg

# Install OpenJDK-8
RUN apt-get update &&\
    apt-get install -y openjdk-8-jdk &&\
    apt-get clean

ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64
ENV PATH $JAVA_HOME/bin:$PATH

# Install scala 2.12
RUN wget https://downloads.lightbend.com/scala/2.12.3/scala-2.12.3.deb && \
    dpkg -i scala-2.12.3.deb && \
    apt-get update && \
    apt-get install -y scala

# Install SBT
# https://www.scala-sbt.org/1.x/docs/Installing-sbt-on-Linux.html
RUN echo "deb http://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list &&\
    curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | apt-key add &&\
    apt-get update &&\
    apt-get install -y sbt

# Install NPM
# RUN curl -fsSL "https://deb.nodesource.com/setup_current.x" | bash -
RUN curl -sL https://deb.nodesource.com/setup_current.x | bash -
RUN apt install -y build-essential -y &&\
    apt-get update &&\
    apt install -y nodejs
RUN apt-get update && npm install -g npm@latest

# for React
EXPOSE 3000
# for playfraework
EXPOSE 9000

# add user with root right
# https://dev.to/emmanuelnk/using-sudo-without-password-prompt-as-non-root-docker-user-52bg
RUN adduser --disabled-password \
    --gecos '' wladyga
RUN adduser wladyga sudo
RUN echo '%sudo ALL=(ALL) NOPASSWD:ALL' >> \
    /etc/sudoers
USER wladyga
WORKDIR /home/wladyga/

# create folders
RUN mkdir ~/scala/ ~/react/ ~/data/
#RUN mkdir /home/wladyga/scala/ /home/wladyga/react/ /home/wladyga/data/

# add trade volume
VOLUME ["/home/wladyga/data/"]