build:
	./build.sh
run-core:
	java -cp "bin:lib/Config.jar" core.simulation.Boot

run-raspbian:
	java -cp "bin:lib/Config.jar" core.simulation.RaspbianWrapperMain

clean:
	rm -r bin
