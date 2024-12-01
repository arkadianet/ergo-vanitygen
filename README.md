<h1>Ergo vanitygen</h1>

<pre>
Usage: java -jar ergo-vanitygen-VERSION.jar [options]
-s, --start              look for pattern at the start of addresses
-e, --end                look for pattern at the end of addresses
-m, --matchCase          match provided pattern with case sensitivity
-p, --pattern [value]    pattern to look for in addresses
--w12                    generate 12-word seed phrases (default is 24)
</pre>

Example:
<br>
`java -jar ergo-vanitygen-1.1.jar -e -p heLLo -m --w12`
<br>
This example will try finding an address that ends exactly with "heLLo" (case-sensitive) using 12-word seed phrases

<b>WARNING</b>: Randomly guessing seeds is demanding for the CPU: finding a 5 letter value can take millions of guesses!
