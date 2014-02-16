$admin_password = '999admin'

class { "splunk":
  install        => "server",
  install_source => "/vagrant/splunk/assets/splunk-6.0.1-189883-linux-2.6-x86_64.rpm",
  admin_password => $admin_password,
}

firewall { '100 allow splunk access':
  port   => [8000, 8089],
  proto  => tcp,
  action => accept,
}

# file { "/vagrant/splunk/assets/outputs/logs/import":
#  source  => "/vagrant/splunk/assets/outputs/logs/archive",
#  ensure  => directory,
#  recurse => true,
#  before  => Class['splunk'],
# }

exec { 'add-license':
  command => 'date',
  path    => '/opt/splunk/bin',
  onlyif  => 'splunk -auth admin:$admin_password add licenses /vagrant/splunk/assets/splunk.license',
  require => Class['splunk'],
}

file { 'bitcoinapp':
  ensure  => present,
  path    => '/opt/splunk/etc/apps/bitcoin',
  source  => '/vagrant/splunk/apps/bitcoin',
  recurse => true,
  require => Class['splunk'],
}

exec { 'restart-splunk':
  command => '/opt/splunk/bin/splunk restart',
  require => Exec['add-license'],
}
