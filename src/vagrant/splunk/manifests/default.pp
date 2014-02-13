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

file { "/vagrant/splunk/assets/outputs/logs/import":
  source  => "/vagrant/splunk/assets/outputs/logs/archive",
  recurse => true,
  require => Class['splunk'],
}

file { 'app-local-dir':
  ensure  => directory,
  path    => '/opt/splunk/etc/apps/launcher/local',
  require => Class['splunk'],
}

file { 'inputs.conf':
  ensure  => present,
  path    => '/opt/splunk/etc/apps/launcher/local/inputs.conf',
  content => template('/vagrant/splunk/templates/site/inputs.conf.erb'),
  mode    => 644,
  require => File['app-local-dir'],
}

exec { 'reload-inputs-conf':
  command => "curl https://localhost:8089/services/data/inputs/monitor/_reload --insecure --request POST -u admin:$admin_password",
  require => File['inputs.conf'],
  path    => ['/usr/bin'],
}

exec { 'add-license':
  command => 'date',
  path    => '/opt/splunk/bin',
  onlyif  => 'splunk -auth admin:$admin_password add licenses /vagrant/splunk/assets/splunk.license',
  require => Class['splunk'],
}

exec { 'restart-splunk':
  command => '/opt/splunk/bin/splunk restart',
  require => Exec['add-license'],
}
