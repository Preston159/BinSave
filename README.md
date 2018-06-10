# BinSave

A library for storing and reading data in a concise binary format

# Code Example

## Import necessary classes

```java
import java.io.File;

import com.preston159.binsave.Save;
import com.preston159.binsave.Data;
import com.preston159.binsave.DataType;
```

## Create a save object

```java
Save s = new Save(new File("file.bin"), new Data("hp", DataType.UINT_8BIT, 1));
```

Instantiating a `Save` object automatically loads the data from the file, or creates the file if it does not exist.

## Store data

```java
s.storeUint("hp", 255);
```

## Retrieve data

```java
int hp = s.getUint("hp");
```

## Sava data to file

```java
s.store();
```

It is recommended to not reference the `Save` object constantly while the program is running.  Instead, use the API to load the data into your own data structure on launch, and save the data using the `Save` object when necessary (e.g. on program close).

# Future plans

- Add more data types
  - Floats
  - Doubles
  - Longs
- Add save file versions to allow format changes. Currently, any expansion must be done by adding new data to the end of the file.

# Release

[Latest](./BinSave/Release/0.0.2.jar)

[0.0.2](./BinSave/Release/0.0.2.jar)

[0.0.1](./BinSave/Release/0.0.1.jar) NOTE: INTS AND UINTS ARE BROKEN IN THIS VERSION

# API Reference

[Latest](http://preston159.com/docs/BinSave/0.0.1/)

[0.0.1](http://preston159.com/docs/BinSave/0.0.1/)

# License

MIT &copy; Preston Petrie
