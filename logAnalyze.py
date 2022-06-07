import pandas as pd
import matplotlib.pyplot as plt
import csv
from os.path import exists

filename = "LOG.CSV"
fields = []
rows = []
names = ["Time (hrs)","TempC","TempF","dhtTemp","dhtHumidity","TVOC (ppb)","eCO2 (ppm)","RawH2","RawEthanol"]

# Imports data from csv log file
'''
def importData():
    if (exists(filename)):
        with open(filename, "r") as csvfile:
            csvreader = csv.reader(csvfile) # Creates csvreader object
            fields = next(csvreader) # Extracts fields
            
            # Extracts each row indivudally into rows
            for row in csvreader:
                rows.append(row)
            
            # Outputs number of data rows
            print("Total no. of rows: %d"%(csvreader.line_num))
        
        # Outputs fields
        print("Field names are:" + ", ".join(field for field in fields))
        return True
    else:
        print("Log file log.csv not found")
        return False
'''

# MAIN
def main():
    '''importSuccess = importData()
    if (not importSuccess):
        exit(1)
    print(rows[0])'''
    
    # Imports dataframe from CSV file
    df = pd.read_csv(filename, names=names)
    print(df.head())
    
    # Modifies dataframe
    df[names[0]] = df[names[0]] / 60000 # Converts time to min
    df[names[0]] = df[names[0]] / 60    # Converts time to hrs
    df.set_index(names[0], inplace=True)
    #df.drop(names[1], 1, inplace=True)
    #df.drop(names[2], 1, inplace=True)
    #df.drop(names[3], 1, inplace=True)
    #df.drop(names[4], 1, inplace=True)
    #df.drop(names[5], 1, inplace=True)
    #df.drop(names[6], 1, inplace=True)
    df.drop(names[7], 1, inplace=True)
    df.drop(names[8], 1, inplace=True)
    
    # Plots df
    df.plot()
    plt.show()
    
# Dunder main
if (__name__ == "__main__"):
    main()