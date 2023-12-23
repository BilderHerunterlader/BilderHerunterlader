# Changelog

## Version 5:
- **Java 11 is required**
- Self written Host Classes might not be compatible anymore, because of HttpClient upgrade and other changes. But they should be made compatible with minimal effort.
- In a rule it is now possible to store variables in a Regex Pipeline and use the variable in search patterns in following Regex definitions. This should be used only if it is really needed.

## Version 4:
- Increased performance by using unsynchronized Collections
- Usage of Interfaces for Collections instead of implementations
- Optimized search for already downloaded files
- Added generic recursive link/image extract functionality for use in HostClasses
- Renamed packages: This makes HostClasses and saved Queues and Keywords incompatible, but they will be loaded anyway and converted to the new version.
- Added HTML escape and unescape functionality
- Improved content-disposition filename extraction
- Removed Splashscreen
- Bug Fixed: ClipboardObverser now detects changes of the clipboard as it should
- DirectoryLog is read the first time the tab is selected and not at program start
- Rules and HostClasses can be enabled and disabled
- ThreadURL, DownloadURL and ThumbnailURL are now also saved in the download logfile
- Standard notification of TrayIcon instead of an own window
- GUI improvements
- **Java 7 is required**
- Lots of small bug fixes
- Added Javascript Rule Pipelines
