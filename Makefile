build:
	./build.sh
run:
	java -cp "bin:lib/Config.jar" core.simulation.Boot
clean:
	rm -r bin
