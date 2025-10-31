FROM sphinxdoc/sphinx-latexpdf:5.3.0

WORKDIR /docs

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update \
 && apt-get install --no-install-recommends -y \
        git \
        librsvg2-bin \
        npm \
 && apt-get autoremove \
 && apt-get clean \
 && rm -rf /var/lib/apb/lists/*

RUN git config --global safe.directory '*'

RUN npm i wavedrom-cli -g

# source /docs/bin/setup_env.sh
ENV PATH="$PATH:/docs/bin"

ADD requirements.txt /docs
RUN pip3 install -r requirements.txt
