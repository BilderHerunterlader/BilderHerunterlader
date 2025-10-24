# Changelog

## Version 5:
- **Java 21 is required!**
- Display an error, when settings file can't be read and let user decide to replace with default settings or exit program
- Store download logs in database (Text-File is still written too). This improves performance when checking for already downloaded URLs and in display of table.
- Store blacklist in database
- Display SVG Icons
- Refactored update code. Updates should now work without write rights in application folder. Updates are downloaded to temporary directory and then installed by a separate updater executable, which asks to run with admin rights.
- Removed NonMultiThreadHttpClient
- Replaced NanoHttp by Grizzly-Http-Server
- Removed backward compatibility with old settings format
- Bug Fixed: Saved last used export path was not used in select dialog
- Fixed encoding issues, because since Java18 Charset.defaultCharset is now returning UTF-8 instead of the native encoding of the operating system
- Lots of smaller Sonar / Bug Fixes

## Version 5:
- **Java 11 is required**
- Self written Host Classes might not be compatible anymore, because of HttpClient upgrade and other changes. But they should be made compatible with minimal effort.
- In a rule it is now possible to store variables in a Regex Pipeline and use the variable in search patterns in following Regex definitions. This should be used only if it is really needed.
- Updated libraries (Most notably Apache httpclient 5.3 and SLF4J 2)
- Added new downloadContainerPage and downloadContainerPageEx methods in Hoster / Host to pass a HttpContext
- Added method to CookieManager to fill httpclient CookieStore
- Rules will now use HttpContext created by HTTPFileDownloader, so that cookies are preserved for all requests for determining download URL and the download itself
- Added constants for the most common info keys in URLParseObject
- Bug Fixed: HTTPFileDownloader did not respect "sendCookies" Flag. Note: Cookies, which were already stored in the cookie store of HttpContext will still be sent, even if "sendCookies" is false.
- Removed backward compatibility for Rule XML Files in old format
- Added new menu items to Help-Menu
- Lots of smaller Sonar / Bug Fixes

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
