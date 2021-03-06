require 'java'
require 'open-uri'

module Atlas

  def self.stringify h
    h.inject(Hash.new) do |a, (k, v)|
      a[k.to_s] = v.to_s
      a
    end
  end

  def self.parse_system path, name="__ROOT__"
    Array(RootSystemParser.new(name, path).__parse)
  end

  def self.parse_install_list xs

    # handle case of ["uri:stuff", { :param => 7}] is single uri
    _, p = Array(xs)
    if p and p.is_a? Hash
      xs = [xs]
    end

    Array(xs).map do |x|
      i, params = Array(x)
      params = if params then
                 Atlas.stringify params
               else
                 Hash.new
               end
      com.ning.atlas.spi.Uri.value_of2(i, params)
    end
  end

  def self.parse_env path
    RootEnvParser.new(path).__parse
  end

  def self.parse_descriptor path
    
    sys = RootSystemParser.new("__ROOT__", path).__parse
    env = RootEnvParser.new(path).__parse
    com.ning.atlas.Descriptor.new(Array(sys), Array(env))
  end

  #
  # Environment Parsing Stuff
  #

  class RootEnvParser
    def initialize path
      @path = path
      @template = open(path).read
      @env = []
    end

    def __parse
      eval @template, binding, @path, 1
      @env
    end

    def environment name, &block
      # name is unused, is there for pretty readability
      @env << EnvironmentParser.new(name, block).__parse
    end

    def system *args
      # ignored
    end

    def server *args
      # ignored
    end
  end

  class EnvironmentParser

    def initialize name, block
      @name = name
      @block = block
      @properties, @provisioners, @installers, @bases, @listeners, @virtual_installers = {}, {}, {}, {}, {}, {}
      @children = []
      @imported_envs = []
      @cardinalities = {}
    end

    def __parse
      instance_eval &@block
      com.ning.atlas.Environment.new(@name,
                                     com.ning.atlas.plugin.StaticPluginSystem.new,
                                     @provisioners,
                                     @installers,
                                     @virtual_installers,
                                     @listeners,
                                     @bases,
                                     @properties,
                                     @children,
                                     @imported_envs,
                                     @cardinalities)
    end

    def cardinality args
      args.each do |k, v|
        if v.respond_to? :to_i
          @cardinalities[k] = v.downto(1).map { |i| (i - 1).to_s }.reverse
        else
          @cardinalities[k] = Array(v)
        end
      end
      @cardinalities.merge(args)
    end

    def import name, args
      # name unused, but left for api similarity with system parsing
      @imported_envs.concat(Atlas.parse_env((args[:url] || args[:source] || args[:src])))
    end

    def base name, args={}
      p, params = Array(args[:provisioner])
      raise "Provisioner URI is required" unless p or args[:inherit]
      params = if params
                 Atlas.stringify params
               else
                 Hash.new
               end

      uri = if p
              com.ning.atlas.spi.Uri.valueOf2(p, params)
            else
              nil
            end

      inits = Atlas.parse_install_list(args[:init])
      if args[:inherit] then
        @bases[name] = com.ning.atlas.Base.new(@bases[args[:inherit]], uri, inits)
      else
        @bases[name] = com.ning.atlas.Base.new(uri, inits)
      end
    end

    def provisioner name, args={}
      @provisioners[name] = Atlas.stringify(args)
    end

    def installer name, args = {}
      if args[:virtual] then
        @virtual_installers[name] = Array(args[:virtual])
      else
        @installers[name] = Atlas.stringify(args)
      end
    end

    def system name="system", args={}, &block
      st = if args[:external] then
             Atlas.parse_system(args[:external], name)
           else
             Array(SystemParser.new(name, args, block).__parse)
           end
      @children.concat(st)
    end

    def server name, args={}
      installers = Atlas.parse_install_list(args[:install])

      # cardinality can be nil, a number, or an array
      cardinality = case it = args[:cardinality]
                      when Array
                        it
                      when Integer
                        it.downto(1).map { |i| i - 1 }.reverse
                      else
                        ["0"]
                    end

      @children << com.ning.atlas.ServerTemplate.new(name,
                                                     com.ning.atlas.spi.Uri.value_of(args[:base].to_s),
                                                     cardinality,
                                                     installers,
                                                     args.inject({}) { |a, (k, v)| a[k.to_s] = v; a })
    end

    alias :service :server

    def listener name, args= {}
      @listeners[name] = Atlas.stringify(args)
    end

    def set args
      @properties.update(Atlas.stringify(args))
    end
  end


  #
  # System Parsing Magic
  #

  class RootSystemParser

    def initialize name, path
      @name, @path = name, path
      @children = []
      @template = open(path).read
    end

    def __parse
      eval @template, binding, @path, 1
      root = com.ning.atlas.SystemTemplate.new @name,
                                               {},
                                               [0],
                                               @children

      if root.type == "__ROOT__"
        root.children
      else
        root
      end
    end

    def server name, args={}
      installers = Atlas.parse_install_list(args[:install])

      # cardinality can be nil, a number, or an array
      cardinality = case it = args[:cardinality]
                      when Array
                        it
                      when Integer
                        it.downto(1).map { |i| i - 1 }.reverse
                      else
                        ["0"]
                    end

      @children << com.ning.atlas.ServerTemplate.new(name,
                                                     com.ning.atlas.spi.Uri.value_of(args[:base].to_s),
                                                     cardinality,
                                                     installers,
                                                     args.inject({}) { |a, (k, v)| a[k.to_s] = v; a })
    end

    def system name="system", args={}, &block
      st = if args[:external] then
             Atlas.parse_system(args[:external], name)
           else
             Array(SystemParser.new(name, args, block).__parse)
           end
      @children.concat(st)
    end


    def environment *args
      # system templates will ignore environment definitions so that you can put them
      # in the same file if you so choose
    end
  end

  class SystemParser

    def initialize name, args, block
      @name, @args, @block = name, args, block
      @children = []
    end

    def __parse
      instance_eval &@block
      # cardinality can be nil, a number, or an array
      cardinality = case it = @args[:cardinality]
                      when Array
                        it
                      when Integer
                        it.downto(1).map { |i| i - 1 }.reverse
                      else
                        ["0"]
                    end
      com.ning.atlas.SystemTemplate.new @name,
                                        @args.inject({}) { |a, (k, v)| a[k.to_s] = v; a },
                                        cardinality,
                                        @children
    end

    def system name="system", args={}, &block
      if args[:external]
        @children.concat(Atlas.parse_system(args[:external], name))
      else
        @children << SystemParser.new(name, args, block).__parse
      end
    end

    def import name, args
      @children.concat(Atlas.parse_system((args[:url] || args[:source] || args[:src]), name))
    end

    def server name, args={}
      installers = Atlas.parse_install_list(args[:install])

      # cardinality can be nil, a number, or an array
      cardinality = case it = args[:cardinality]
                      when Array
                        it
                      when Integer
                        it.downto(1).map { |i| i - 1 }.reverse
                      else
                        ["0"]
                    end

      @children << com.ning.atlas.ServerTemplate.new(name,
                                                     com.ning.atlas.spi.Uri.value_of(args[:base].to_s),
                                                     cardinality,
                                                     installers,
                                                     args.inject({}) { |a, (k, v)| a[k.to_s] = v; a })
    end

    alias :service :server
  end

end
