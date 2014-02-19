$admin_password = '999admin'

class { "splunk":
  install               => "server",
  install_source        => "/vagrant/splunk/assets/splunk-6-linux-2.6-x86_64.rpm",
  license_file_source   => "/vagrant/splunk/assets/splunk.license",
  admin_password        => $admin_password,
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

file { 'bitcoinapp':
  ensure  => link,
  path    => '/opt/splunk/etc/apps/bitcoin',
  target  => '/vagrant/splunk/apps/bitcoin',
  require => Class['splunk'],
}

exec { 'restart-splunk':
  command => '/opt/splunk/bin/splunk restart',
  require => File['bitcoinapp'],
}
