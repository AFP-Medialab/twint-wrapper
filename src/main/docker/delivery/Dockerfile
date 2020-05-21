FROM twintplus:0.2 as base

# install openjre:8
RUN mkdir -p /usr/share/man/man1 && apt install software-properties-common -y && apt update && wget -qO - https://adoptopenjdk.jfrog.io/adoptopenjdk/api/gpg/key/public | apt-key add -\
	&& add-apt-repository --yes https://adoptopenjdk.jfrog.io/adoptopenjdk/deb/ && apt update && apt install adoptopenjdk-8-hotspot -y

FROM base

COPY twint-wrapper.jar twint-wrapper.jar 

ENV PROFILE=prod

ENTRYPOINT [ "/bin/bash", "-c", "java  -Djava.security.egd=file:/dev/./urandom -jar -Dspring.profiles.active=$PROFILE twint-wrapper.jar"]

CMD ["/bin/bash"]