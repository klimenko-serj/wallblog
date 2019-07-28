dev-js:
	cd frontend && npm run dev

dev-scala:
	sbt compile

dev: dev-js dev-scala

dev-watch:
	(cd frontend && npm run dev-watch) & \
	sbt run

prod-js:
	cd frontend && npm run build

prod-scala:
	rm -rf ./dist && \
	sbt dist && \
	unzip target/universal/wallblog-1.0-SNAPSHOT.zip -d ./dist && \
	mv -f ./dist/wallblog* ./dist/wallblog && \
	rm ./dist/wallblog/bin/*.bat && \
	chmod +x ./dist/wallblog/bin/wallblog

prod: prod-js prod-scala
