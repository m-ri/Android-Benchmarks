# Android-Benchmarks

The goal of this app is to perform some benchmarks over Android.
Actually I'm performing benchmarks about SQLite's insertion and try/catch in different scenarios. Results are visible on the screen, over adb and saved on external storage (see below)

- SQLite Insert. There are several trials, with different number of numerical(integer) and litteral colmns. Following test are performed:
  - SimpleInsertion : Are executed queries like 'INSERT INTO table (column1) VALUES ('aaaa')'
  - CompiledInsertion: An insert query is compiled, after that it is launched N times, where Nis the number of rows
  - CompiledInsertion_groupedRows: it is like CompiledInsertion, but multiple record are inserted each time.

- TryCatch: an exception is thrown several time. Different types of try..catch blocks are tested:
  - conditionalThrow_1_level : single try and single catch
  - conditionalThrow_10_levels_seq : single try, where exceptions are captured by the 10^ catch block (try{..}catch(e1){..}catch(e2){}..
  - conditionalThrow_10_levels_nested : like conditionalThrow_10_levels_seq, but there are 10 nested try..catch block(try{try{try..}catch(e1..)

Results are stored inside /resultsBenchmarks folder

Please run the .apk over your phone, take the file saved in "external storage"->Android->data->marco.mrm.trybenchmarks->files and commit it to /resultsBenchmarks folder
