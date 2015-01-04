#! /bin/bash
function setup(){
	log_file="leotask.log";
	
	i=1;
	while [ -f "$log_file" ]
	do
		log_file="leotask$i.log";
		i=$(( $i + 1 ))
	done
	
	printf "log:$log_file\n";
}

declare -a tasks=('tasks-confickerlike-2020.xml' 'tasks-confickerlike-2020-2.xml' 'tasks-confickerlike-2020-diurnal.xml');
setup $1;
title="Run-Batch[${#tasks[@]}]:${tasks[@]}\n";
#You have to use "" to surround str varaibles in printf to prevent the printing from stopping at the a blank in the str.
printf "$title";printf "$title" > $log_file;
li=0;
while (($li<${#tasks[@]})) 
	do	
		let lia=li+1;
		title_task="Run-$lia/${#tasks[@]}:${tasks[$li]}\n";
		printf "$title_task";printf "$title_task" >> $log_file;	
		cat ${tasks[$li]} >> $log_file;
		#printf "\n" >> $log_file;
		java -server -Xmx3G -jar leotask.jar -load=${tasks[$li]} >> $log_file &
		pid=$!
		wait $pid
		#printf "\n" >> $log_file;
		let li=li+1;
	done
title_finish="Run-Batch Finished!\n";
printf "$title_finish";printf "$title_finish" >> $log_file;
