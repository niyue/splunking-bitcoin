Listening to Bitcoin network with Splunk
==============================
This is a simple system for [Splunking](http://www.splunk.com) [Bitcoin](https://bitcoin.org/) network and showing interesting metrics for Bitcoin economy. 

It is consisted of two components:

* A Bitcoin node for downloading Bitcoin block chain and listening Bitcoin block chain events via the Bitcoin P2P network
* A [Vagrant](http://www.vagrantup.com/) nvironment for setting up Splunk and importing the Bitcoin logging data generated by the Bitcoin node above

To set it up (only verified under Mac OS X 10.9):

* git clone this Github repository
	* let's call the repository folder SPLUNKING_BITCOIN_HOME
	* and call the SPLUNKING_BITCOIN_HOME/src/vagrant/splunk/assets folder as ASSETS folder
* Download [Splunk](http://www.splunk.com/download) 
	* you need to download the Linux x64 for RedHat edition (an RPM package)
	* put the downloaded RPM package under the ASSETS folder
* Download Bitcoin [bootstrap.dat](http://sourceforge.net/projects/bitcoin/files/Bitcoin/blockchain/) file
	* this is used for fast syncing the block chain
	* you need to use some BitTorrrent software to download it 
	* please put it under the ASSETS folder
* Download and install [Vagrant](http://www.vagrantup.com/)
* Download and install [Apache Maven](http://maven.apache.org)
* Start the Bitcoin node for block chain syncing and listening
	* Enter SPLUNKING_BITCOIN_HOME directory, run "mvn exec:java"
* Install Splunk license
	* Copy your Splunk license file to the ASSETS folder and name it splunk.license
* Use vagrant to set up the entire environment
	* Enter SPLUNKING_BITCOIN_HOME/src/vagrant directory, run "vagrant up"
* That's it
	* you can use your browser to nagivate to http://192.168.33.9:8000 (admin:999admin) to use Splunk Web to explore the data

Some reports
=================
* Transaction malleability attack visualized hour by hour
![Transaction malleability attack visualized hour by hour](static/images/tx_malleability_attack_hour_by_hour.png)

* Transaction malleability attack prototype identified
![Transaction malleability attack prototype identified](static/images/tx_malleability_attack_close_look.png)

* Linear speed block chain height increasement
![Linear speed block chain height increasement](static/images/block_chain_height.png)
