# FileRegexParser Binding

The FileRegexParser binding provides thing to read new lines from a file, parse them with a given regular expression and provide channels for each matching group.

Example usage:
- write system load to a file
- use FileRegexParser to monitor the system load in OpenHAB2 
(see full example)

## Supported Things

This binding supports one thing: fileregexparser

## Discovery

Discovery is not applicable.

## Binding Configuration

No binding configuration required.

## Thing Configuration

The things requires the file name (fileName including full path) and the regular expression to be applied to each line (regEx)
For further information on how to define the regular expressions please see: https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html

## Channels

+ groupCount type Number: The number of matching groups found in the regEx
+ matchingGroupX type String: For each matching group a channel with the name "matchingGroupX" (where X is the number of the matching group in the regEx) is provided.

## Full Example

This example will get the load avarage of the linux system every minute and update the related items.

Crontab:
```
* * * * * openhab cat /proc/loadavg >> /tmp/openhab/loadavg.txt
```

Things:

```
fileregexparser:filetoparse:loadAvg [fileName="/tmp/openhab/loadavg.txt", regEx="^(\\d{0,2}\\.\\d{2})\\s(\\d{0,2}\\.\\d{2})\\s(\\d{0,2}\\.\\d{2})\\s(\\d*/\\d*)\\s(\\d*)$"]

```

Items:

```
Number sysNoMatchingGroups    "Number of matching groups [%d]" (gSys) {channel="fileregexparser:filetoparse:loadAvg:groupCount"}
String sysLoadAvg1  "LoadAvg 1min [%s]"  (gSys) { channel="fileregexparser:filetoparse:loadAvg:matchingGroup1" }
String sysLoadAvg5  "LoadAvg 5min [%s]"  (gSys) { channel="fileregexparser:filetoparse:loadAvg:matchingGroup2" }
String sysLoadAvg15  "LoadAvg 15min [%s]"  (gSys) { channel="fileregexparser:filetoparse:loadAvg:matchingGroup3" }
String sysThreads  "Threads [%s]"  (gSys) { channel="fileregexparser:filetoparse:loadAvg:matchingGroup4" }
String sysLastPid  "Last PID [%d]"  (gSys) { channel="fileregexparser:filetoparse:loadAvg:matchingGroup5" }
```
