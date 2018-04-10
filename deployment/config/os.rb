set :oddrun_block_openner, '#ODDRUN TWEAKS'
set :oddrun_block_closer, '#END OF ODDRUN TWEAKS'

set :sysctl_path, '/etc/sysctl.conf'
set :sysctl, 'net.ipv4.ip_local_port_range = 2048 65535
net.core.somaxconn = 12000
net.ipv4.tcp_fin_timeout = 3
net.ipv4.tcp_tw_reuse = 0
net.ipv4.tcp_tw_recycle = 0
net.ipv4.tcp_max_tw_buckets = 1000000
net.core.netdev_max_backlog = 4096
net.ipv4.tcp_syncookies = 1
net.ipv4.tcp_max_syn_backlog = 4096
net.ipv4.tcp_synack_retries = 3
net.ipv4.tcp_syn_retries = 3
net.ipv4.tcp_retries1 = 1
net.ipv4.tcp_retries2 = 2
net.ipv4.tcp_orphan_retries = 0
net.ipv4.tcp_no_metrics_save = 1
net.core.wmem_max = 8388608
net.core.rmem_max = 8388608
net.ipv4.tcp_rmem = 4096  87380 8388608
net.ipv4.tcp_wmem = 4096    87380   8388608
net.nf_conntrack_max = 1024000
net.netfilter.nf_conntrack_max = 1024000
net.netfilter.nf_conntrack_buckets = 128000
net.ipv4.netfilter.ip_conntrack_tcp_timeout_established = 18000
fs.file-max = 512000
vm.overcommit_memory = 1'

set :limits_path, '/etc/security/limits.conf'
set :limits, '
root  soft  nofile    512000
root  hard  nofile    512000
root  soft  nproc     64000
root  hard  nproc     64000'
