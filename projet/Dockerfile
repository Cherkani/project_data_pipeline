FROM apache/flink:1.17.1

# Install Python 3 and pip
RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    rm -rf /var/lib/apt/lists/*

# Install PyFlink
RUN pip3 install apache-flink
