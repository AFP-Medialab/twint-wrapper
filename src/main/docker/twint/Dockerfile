FROM python:3.6-slim as base

FROM base as builder

MAINTAINER Bertrand Goupil (bertrand.goupil@afp.com)

RUN mkdir /install
WORKDIR /install

RUN apt-get update && apt-get install -y --no-install-recommends \
	git && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*
	
RUN git clone https://github.com/AFP-Medialab/twint.git twint
#RUN cd twint && python setup.py install && cd .. && rm -rf /twint
RUN mkdir -p build/lib/python3.6/site-packages
ENV PYTHONPATH=/install/build/lib/python3.6/site-packages/
RUN cd twint && python setup.py install --prefix=/install/build

FROM base

COPY --from=builder /install/build /usr/local/

ENTRYPOINT ["/bin/sh", "-c"]
CMD ["/bin/bash"]