#! /bin/bash
function setup(){
	log_file="leotask.log";
	task="tasks.xml"
	
	i=1;
	while [ -f "$log_file" ]
	do
		log_file="leotask$i.log";
		i=$(( $i + 1 ))
	done
	
	printf "log:$log_file\n";
	
	if [[ $1 != "" ]]
	then
		task=$1
	fi
}

setup $1;
title="Run:$task\n";
printf "$title";printf "$title" > $log_file;
cat $task;cat $task >> $log_file;
#printf "\n";printf "\n" >> $log_file;
java -server -Xmx3G -jar leotask.jar -load=$task
