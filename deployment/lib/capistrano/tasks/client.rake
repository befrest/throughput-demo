namespace :client do

  desc 'subscribe 100k clients'
  task :start_100k do
    node_share = 10 / roles(:clients_simulator_node).size

    on roles(:clients_simulator_node) do
      execute("cd; nohup ./client.py demo #{node_share} 1>stdout 2>stderr &")
    end
  end

  desc 'Kill all clients'
  task :kill_all do
    on roles(:clients_simulator_node) do
      execute('cd; kill -9 $(cat client.pid)')
    end
  end

end
