#
#	Implemented by the Computer Systems Lab, University of Thessaly (https://csl.e-ce.uth.gr)
#	for the MLSysOps project (https://mlsysops.eu)
#	 
#	License: LGPL - https://www.gnu.org/licenses/lgpl-3.0.en.html
#	 
#	Copyright (c) 2024, The University of Thessaly, Greece
#	Contact: Christos Antonopoulos  cda@uth.gr
#
# This software is licensed for research and non-commercial use only.
# 
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.

import os
import glob
import shutil

import re

def natural_sort(l): 
    convert = lambda text: int(text) if text.isdigit() else text.lower() 
    alphanum_key = lambda key: [ convert(c) for c in re.split('([0-9]+)', key) ] 
    return sorted(l, key = alphanum_key)

#define the path for the input and output folders
usage_folder="/home/chris/traces/task_usage/"
events_folder="/home/chris/traces/task_events/"
output_folder ="/home/chris/traces/output"

if not os.path.exists(output_folder):
    os.mkdir(output_folder)
else:
    for the_file in os.listdir(output_folder):
        file_path = os.path.join(output_folder, the_file)
        try:
            if os.path.isfile(file_path):
                os.unlink(file_path)
            elif os.path.isdir(file_path): 
                shutil.rmtree(file_path)
        except Exception as e:
            print(e)

print "Load extraction"
#extract the resource usage for vms per period from task_usage
for ufile in natural_sort(glob.glob(events_folder+"/part-*-of-00500.csv")):
    print ufile
    lines = [x.replace("\n","").split(",") for x in open(ufile,"r")]
    for idx, x in enumerate(lines):
        
        if x[1] != "":
            continue
        
        filename= x[2]+"_"+x[3]
        
        try:
            
            if float(x[10]) == 0 or float(x[9]) == 0:
                continue

            fd = open(output_folder+os.sep+filename+"_load","a")
            fd.write(",".join([str(x) for x in [x[0],filename, "{:20.10f}".format(float(x[8])), "{:20.10f}".format(float(x[9])), "{:20.10f}".format(float(x[10]))]])+"\n" )
            fd.close()
        except:
            print idx, "float error"
            print x
            exit(0)

print "Usage extraction"
#extract the resource usage for vms per period from task_usage
for ufile in natural_sort(glob.glob(usage_folder+"/part-*-of-00500.csv")):
    print ufile
    lines = [x.replace("\n","").split(",") for x in open(ufile,"r")]
    for x in lines:
        
        if int(x[0])%300000000 != 0:
            continue

        filename= x[2]+"_"+x[3]

        if not os.path.exists(output_folder+os.sep+filename+"_load"):
            continue

        if x[5].strip() == "" or float(x[5]) == 0.0:
            x[5] = 10e-9
        
        fd = open(output_folder+os.sep+filename+"_usage","a")
        fd.write(",".join([str(x) for x in [x[0],filename, "{:20.10f}".format(float(x[5]))]])+"\n")
        fd.close()


exit(0)
