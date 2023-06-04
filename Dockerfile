FROM sphinxdoc/sphinx

WORKDIR /docs

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update \
 && apt-get install --no-install-recommends -y \
        git \
 && apt-get autoremove \
 && apt-get clean \
 && rm -rf /var/lib/apb/lists/*

# source /docs/bin/setup_env.sh
ENV PATH="$PATH:/docs/bin"

ADD requirements.txt /docs
RUN pip3 install -r requirements.txt
