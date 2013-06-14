The tip_of_the_day.json file expects a JSON formatted array of objects.  Each
object is a tip of the day. Tips of the day have 2 required fields and 2 optional
fields.  

infoText: 
		A string describing what the tip is

linkText: 
		A string with one or more hrefs in it that allows users to invoke an action.
		An action is one of the following:
				* a standard url, linking to an external website.  Use http://
				* a link to a preferences page, use pref:PAGE_ID
				* a command to execute, use command:COMMAND_ID

keyBindingId:
		The id of a keybinding to display in the tip.  This is typically the same as the 
		command id above

required:
		the id of a bundle that must be installed in order for this tip to be relevant.
		If the bundle is not installed, this tip will not be shown.


Here is an example array showing all possibilities:

[
	{
		"infoText":"Do you want to make Eclipse better?",
		"linkText":"<a href=\"pref:org.springsource.ide.eclipse.commons.curatorPreferencesPage\">Open curated preferences page</a>"
	},
	{
		"infoText":"Check out the latest info on SpringFramework",
		"linkText":"Go to <a href=\"http://springsource.org\">SpringSource</a> website."
	},
	{
		"infoText":"Do you know about the new quick search feature?",
		"linkText":"<a href=\"command:org.springsource.ide.eclipse.quicksearch.commands.quicksearchCommand\">Try it out.</a>",
		"keyBindingId":"org.springsource.ide.eclipse.quicksearch.commands.quicksearchCommand",
		"required":"org.springsource.ide.eclipse.commons.quicksearch"
	}
]