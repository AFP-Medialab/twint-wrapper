FROM twintplus:0.9 as base

# install openjre:8
RUN mkdir -p /usr/share/man/man1 && apt install openjdk-11-jre -y && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

FROM base

COPY twint-wrapper.jar twint-wrapper.jar 

ENV PROFILE=prod

ENTRYPOINT [ "/bin/bash", "-c", "java  -Djava.security.egd=file:/dev/./urandom -jar -Dspring.profiles.active=$PROFILE twint-wrapper.jar"]

CMD ["/bin/bash"]