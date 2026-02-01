# Simple File Sorter

A lightweight utility to organize photos and videos into a **year/month directory structure**.

## Features
- Extracts the creation date from file **metadata** when available  
- Falls back to **parsing the date from the filename** if metadata is missing  
- Designed to efficiently handle **large batches of files**

The tool has been tested on collections of ~2000 photos and videos (several GB), completing the sorting process in a matter of seconds. (4/5 second on average)

For large datasets, **disabling logging may improve performance** by reducing I/O overhead.  
Actual performance may vary depending on the environment and filesystem.

## Configuration
To run the application, only **three paths** need to be configured in `application.properties`:

- Input directory  
- Output directory  
- Log destination path

No other configuration is required.

## Requirements & Execution
- **Java 17**
- Can be run directly from **IntelliJ IDEA** or any other Java-compatible IDE
- No external runtime dependencies required

## Notes
When processing very large volumes of files, consider disabling logging to reduce overhead and improve execution time.
