namespace :client do

  desc 'Update client code'
  task :update do
    on roles(:clients_simulator_node) do
      upload! '/home/mehdi/projects/befrest-demo/client/client.py', '/root/client.py'
    end
  end

  desc 'subscribe n thousands clients'
  task :subscribe, :sc do |t, args|
    node_share = args[:sc].to_i / roles(:clients_simulator_node).size

    on roles(:clients_simulator_node) do
      execute("cd; nohup ./client.py demo #{node_share} 1>stdout 2>stderr &")
    end
  end

  desc 'subscribe one channel per host'
  task :subscribe_1channel_1client, :prefix do |t, args|
    prefix = args[:prefix]
    counter = 1
    hosts = roles(:clients_simulator_node)

    hosts.each { |h|
      on h do
        execute("cd; nohup ./client.py #{args[:prefix]}#{counter} 1 1>stdout 2>stderr &")
      end

      counter = counter + 1
    }
  end

  desc 'Kill all clients'
  task :kill_all do
    on roles(:clients_simulator_node) do
      execute('cd; kill -9 $(pgrep client.py -d " ")')
    end
  end

end
