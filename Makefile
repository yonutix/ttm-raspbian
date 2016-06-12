build:
	./build.sh
run-core:
	java -cp "bin:lib/Config.jar" core.simulation.Boot

run-raspbian:
	sudo java -cp "bin:lib/Config.jar:lib/pi4j-core.jar" core.simulation.RaspbianWrapperMain

clean:
	rm -r bin
