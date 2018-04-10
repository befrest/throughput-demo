namespace :os do

  desc 'Make required os tunnings'
  task :tune do
    sysctl_path = fetch(:sysctl_path)
    limits_path = fetch(:limits_path)
    block_openner = fetch(:oddrun_block_openner)
    block_closer = fetch(:oddrun_block_closer)

    on roles(:clients_simulator_node) do
      sysctl = fetch(:sysctl)
      limits = fetch(:limits)

      execute "sed -i '/#{block_openner}/,/#{block_closer}/d' #{sysctl_path}"
      execute :echo, "'#{block_openner}' >>", sysctl_path
      sysctl.each_line do |line|
        if line.include?("\n")
          line["\n"] = ''
        end
        execute :echo, line, '>>', sysctl_path
      end
      execute :echo, "'#{block_closer}' >> ", sysctl_path
      test :sysctl, '-p', sysctl_path

      execute "sed -i '/#{block_openner}/,/#{block_closer}/d' #{limits_path}"
      execute :echo, "'#{block_openner}' >> ", limits_path

      limits.each_line do |line|
        if line.include?("\n")
          line["\n"] = ''
        end
        execute :echo, line, '>>', limits_path
      end

      execute :echo, "'#{block_closer}' >> ", limits_path

    end
  end

end
