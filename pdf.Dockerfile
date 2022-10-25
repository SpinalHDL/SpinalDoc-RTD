FROM sphinxdoc/sphinx-latexpdf

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

RUN npm i wavedrom-cli -g

ADD requirements.txt /docs
RUN pip3 install -r requirements.txt
