<pre>
<b><a name="rules" class="noStyle">{t}Rules{/t}</a></b>
{t}In BH everyone can define his own rules. It is possible to add support for other hosters. But you have to know regular expressions.{/t}
</pre>
<div class="download">
<pre>
<b>{t}Possibility{/t} 1</b>
{t}If it is possible to replace something in the Container-URL or Thumbnail-URL to get the direct URL to the image than you can do so.

You have to create a new rule. First you have to define a pattern for the Container-URL as regular expression, so that the rule knows for which URLs to search.
Now you can make one or more replacements behind the other.
The result from a replacement is used for the next replacement.
Only the first replacement is using the Container-URL or Thumbnail-URL.{/t}

<b>{t}Example{/t}</b>
{t}The Thumbnail-URL is:{/t} http://img411.imageshack.us/img411/5221/935871ia1.th.jpg
{t}The search pattern is:{/t}
http://(img[0-9]*)\.([a-zA-Z0-9.]*)/([a-zA-Z0-9]*)/([a-zA-Z0-9_%]*)/([a-zA-Z0-9-_%]*)\.th\.([a-zA-Z]*)
{t}And the replacement looks like this:{/t}
http://$1.$2/$3/$4/$5.$6
{t}We get the URL:{/t}
http://img411.imageshack.us/img411/5221/935871ia1.jpg
</pre>
</div>

<div class="download">
<pre>
<b>{t}Possibility{/t} 2</b>
{t}You have to create a new rule. First you have to define a pattern for the Container-URL as regular expression, so that the rule knows for which URLs to search.
Then you have to select the second mode.

If the URL to the image can't get from the Container-URL, than you can let the program download the sourcecode of the webpage and you can make replacements in it.
Here you can also do more than one searches behind the other. The result from a search is the position in the sourcecode where the pattern was found.
The next search will use this position as startpoint for the search.
The last search is taking the result and is replacing it.

In the Replacement-Pattern you can use two variables:{/t}
{t}As URL we have:{/t} http://bla.irgendwas.net/ordner/bild.jpg
$SRV {t}is replaced by the domain. So we get{/t} http://bla.irgendwas.net/
$URL {t}is replaced by the URL without the filename. We get{/t} http://bla.irgendwas.net/ordner/

{t}In the Search-Pattern you can use brackets to use the content as reference in the Replace-Pattern.
We take as sourcecode this:{/t}
SRC="aAfkjfp01fo1i-28074/loc164/54394_NoraTschirner_FCVenusPremiere_01.jpg">
{t}As Search-Pattern we take:{/t} SRC="(.*)">
{t}For this bracket you can use the reference $1.
On replacing the program will also replace $1 with the content of the SRC-attribute.
You can use more than one brackets, the reference are then nummbered.
The first bracket is $1, the second is $2 and so on.{/t}

<b>{t}Example{/t}</b>
{t}The source is downloaded first.
As URL we take this:{/t}
http://img7.imagevenue.com/img.php?loc=loc164&amp;image=54394_NoraTschirner_FCVenusPremiere_01.jpg
{t}Here is a part of the sourcecode from imagevenue:{/t}
</pre>
<div class="code">
	{include file="bilderherunterlader/rules_html.tpl" assign="exampleHTML"}
	{geshi geshiInstance=$geshiInstance source=$exampleHTML language="html4strict"}
</div>
<pre>
{t}The first search-pattern looks like this:{/t}
id=&quot;thepic&quot;
{t}The search finds the position of the pattern in the sourcecode.
Now we aren't at the right position, because we want the image, the content in SRC=""
So we need a second search-pattern which looks like this:{/t}
SRC=&quot;(.*)&quot;&gt;
{t}Now we got what we want. In the program the result is written to a variable.
In this variable is now this:{/t}
SRC="aAfkjfp01fo1i-28074/loc164/54394_NoraTschirner_FCVenusPremiere_01.jpg">

{t}Now we have to define a Replace-Pattern in the last search.
In this example we need $SRV$1
The program replaces now the variable with this pattern und we get the URL:{/t}
http://img7.imagevenue.com/aAfkjfp01fo1i-28074/loc164/54394_NoraTschirner_FCVenusPremiere_01.jpg
</pre>
</div>
<pre>
{t}You can now also correct the filename. This is working similar to Possibility 1. But for the search the filename from the Image-URL is used, not the whole URL.

So, if the URL is{/t}
http://img7.imagevenue.com/aAfkjfp01fo1i-28074/loc164/54394_NoraTschirner_FCVenusPremiere_01.jpg
{t}then it is used only{/t} 54394_NoraTschirner_FCVenusPremiere_01.jpg.

<b>{t}Share your rules with others{/t}</b>
{t}If you have defined a rule and want to share it with other users of the program, you can send it to me. I will proof the rule and make it available over the update-function.{/t}

<b>{t}XML-Files of the Rules{/t}</b>
{t}The rules are stored in XML-Files in the subfolder "rules" of the program folder.{/t}

<b><a name="DirectLinkedImages">{t}Directly linked images{/t}</a></b>
{t}If it is nessesary to download directly linked images or other files, you can enable this with regular expressions.
That class detects by default only a small number of URLs. To allow more URLs to be downloaded you have add search patterns in the detection settings.

Here is an example:{/t}
<div class="code">^http:\/\/.*\/.*\.(gif|jpg|jpeg|jpe|png|tif|tiff)
^http:\/\/(.*\.|).*\..*\/.*\?page=Attachment&attachmentID=[0-9]+&h=[0-9a-z]+
</div>
{t}The first expression allows to download file with some endings.
The second expression allows to download attachments in the boardsoftware WBB.
Upper and lower case at this point of the program is not respected.{/t}

<b><a name="hostplugins" class="noStyle">{t}Host-Plugins (Host-Classes){/t}</a></b>
{t}If this possibilities aren't enough and you can program in Java, then you can write your own classes which can be used as Hosters-Plugins.
To compile such a class the sourcecode of the program is required.
Because the class has to extend ch.supertomcat.bh.hoster.Host and has to implement the Interface ch.supertomcat.bh.hoster.IHoster.{/t}
</pre>