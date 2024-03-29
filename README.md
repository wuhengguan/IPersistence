# 第一阶段模块一作业题

### 1、Mybatis动态sql是做什么的？都有哪些动态sql？简述一下动态sql的执行原理？
    （1）mysql动态sql是为了解决根据不同条件拼接sql的问题，动态sql包括条件判断if，循环遍历choose (when, otherwise)和foreach，另外还有trim (where, set)做些防止生成不规范的sql
    （2）执行原理：
        sql语句的存储结构：
        SqlNode接口，每个xml node都会解析成对应的sqlNode，比如有：if标签对应的IfSqlNode，choose标签对应的ChooseSqlNode，foreach标签对应的ForeachSqlNode等
        SqlSource来源接口，它代表从Mapper XML或方法注解上，读取的一条SQL内容，其实现类有：动态sql类DynamicSqlSource，基于方法上的 @ProviderXXX 注解的 SqlSource实现类ProviderSqlSource等
        BoundSql类，一次可执行的SQL封装
        XMLScriptBuilder类，是负责解析各个节点sql部分，比如select,insert,update,delete节点
        NodeHandler接口，是XMLScriptBuilder的内部接口，其中包括IfHandler,ChooseHandler,TrimHandler,WhereHandler,SetHandler等处理器，负责对不同动态sql标签的解析
        
        解析的过程：
        -- 负责把配置的Sql解析成为SqlSource，如果是动态sql则返回DynamicSqlSource
        XMLScriptBuilder.parseScriptNode()
            -- 具体的解析过程，把sql解析为MixedSqlNode，如果类型是Node.CDATA_SECTION_NODE 或者 Node.TEXT_NODE 时解析为TextSqlNode
            -- 如果是Node.ELEMENT_NODE元素类型，则根据子标签类型获得对应的NodeHandler，并且标记sql为动态sql
            -> XMLScriptBuilder.parseDynamicTags()
        
        当根据参数获取BoundSql时：
        -- 根据传入的参数构建BoundSql
        DynamicSqlSource.getBoundSql(Object parameterObject)
            -- 把configuration和parameter封装成DynamicContext传入sql根节点apply方法开始解析 , 
            -- 而rootSqlNode是一个MixedSqlNode，会便利调用内部各个SqlNode的apply方法把sql拼接到DynamicContenx中
            -> rootSqlNode.apply(DynamicContext)
            -- 解析完动态sql后，进行参数解析，也就是对参数占位符进行替换
            --> SqlSourceBuilder.parse(context.getSql(), parameterType, context.getBindings())
            -- 参数和sql都解析完后则构建BoundSql返回使用即可
              
        动态sql的解析主要是根据不同的节点类型使用不同的解析策略，把xml配置的sql解析成多个SqlNode组成的数据结构，根节点是一个MixedSqlNode，
        根节点包含所有的子节点的，而子节点又是根据不同的类型，实现了不同的解析，当调用根节点的MixedSqlNode的apply方法，会循环执行所有子节点的apply方法，
        每个子节点根据自己不同的解析策略解析完sql，拼接成最终sql
        
        
### 2、Mybatis是否支持延迟加载？如果支持，它的实现原理是什么？
    
    mybatis是支持延时加载的，在配置文件lazyLoadingEnabled=true开启，另一个配置aggressiveLazyLoading（当开启时，任何方法的调用都会加载该对象的所有属性。否则，每个属性会按需加载）
    
    实现原理：
    实现主要思路是在返回的结果对象中进行了动态代理，当访问到具体的获取参数方法实际是执行代理类的处理方法去数据库中查询对应的值，然后封装返回
    （1）在查询数据返回的时候，如果开启了懒加载，mybatis会对延迟加载对象进行动态代理返回一个动态代理对象
        -- statement查询数据
        PreparedStatementHandler.query() ->
            -- 查询完成进行结果封装处理                     
            DefaultResultSetHandler.handleResultSets(ps) 
                -- 处理 ResultSet 返回的每一行 Row 
                -> handleRowValues()              
                    -- 处理简单映射的结果
                    -> handleRowValuesForSimpleResultMap() 
                        -- 根据最终确定的 ResultMap 对 ResultSet 中的该行记录进行映射，得到映射后的结果对象
                        -> getRowValue() 
                            -- 创建映射后的结果对象
                            -> createResultObject()
                                -- 如果有内嵌的查询，并且开启延迟加载，则创建结果对象的代理对象
                                -- configuration.getProxyFactory().createProxy()
                                    -- 结果对象的代理对象支持Javassist和CGlib动态代理方式，这里演示JavassistProxyFactory，创建了一个EnhancedResultObjectProxyImpl对象
                                    --> JavassistProxyFactory.createProxy()
                                    -- 通过其内部类的EnhancedResultObjectProxyImpl创建代理对象，并指定执行器为EnhancedResultObjectProxyImpl对象，至此创建结果中的懒加载类对象完成，实际返回的是一个动态代理对象
                                        ->JavassistProxyFactory.EnhancedResultObjectProxyImpl.createProxy()
                                    
    （2）当访问到配置了懒加载的属性值（lazyLoadingEnabled=true, aggressiveLazyLoading=false的情况，如果aggressiveLazyLoading=true当访问结果详情的时候就会对懒加载属性进行加载）
        -- 访问懒加载对象属性的方法，实际就是执行了代理对象执行器EnhancedResultObjectProxyImpl中的invoke的方法
        JavassistProxyFactory.EnhancedResultObjectProxyImpl.invoke()
            -- 如果调用了 getting 方法，则执行延迟加载，延迟加载是通过EnhancedResultObjectProxyImpl对象的内部类ResultLoaderMap进行加载的（ResultLoaderMap是在最终创建代理对象时，会作为参数传入代理，上面查询的时候最后创建代理对象的时候传入了）
            -> ResultLoaderMap.load()
                -- ResultLoaderMap内部通过一个HashMap维护一个Map<String, LoadPair> loaderMap映射，当延迟加载完成则会删掉对应的key，key为属性名称，load是通过map中的值LoadPair进行具体的查询
                -> LoadPair.load()
                    -- 通过LoadPair内部的ResultLoader的loadResult继续查询
                    -> ResultLoader.loadResult()
                        -- ResultLoader内部的selectList()进行查询，获取到内部的Executor，然后通过Executor的query方法进行查询
                        -> ResultLoader.selectList()
                            -- 查询完成进行提取结果
                            -> ResultLoader.extractObjectFromList()
### 3、Mybatis都有哪些Executor执行器？它们之间的区别是什么？
    mybatis的Executor使用了模板模式，主要有以下几种：
    BaseExecutor：
        该类主要使用了模板模式，把Executor的共性封装在其中，特性化的内容由其子类实现
    SimpleExecutor：
        普通的执行器，普通就在于每一次执行都会创建一个新的Statement对象，用完立刻关闭Statement对象。
    ReuseExecutor：
        执行器会重用预处理语句，也就是不会每一次调用就创建一个Statement，在一个会话中会利用前面已经创建好的Statement，但前提是执行的sql一样，这里类似数据库连接池中的PSCache，但是这里的很局限，只有一次会话中可以共享。由于共享了Statement，所以性能比SimpleExecutor高
    BatchExecutor：
        该类的特性就是用了Statement的addBatch方法，并没有使用PSCache，仍然是一次调用执行创建一个Statement对象，所以性能不一定比ReuseExecutor高
    CachingExecutor：
        这个类并没有基础BaseExecutor基础类，而是采用了一个装饰器模式，主要作用是实现了二级缓存，当全局开启了二级缓存时会使用该Executor
    
### 4、简述下Mybatis的一级、二级缓存（分别从存储结构、范围、失效场景。三个方面来作答）？
    一级缓存：
        一级缓存是SqlSession级别的缓存，只在会话中有效，使用一个HashMap存储了查询数据，
        当同一个会话下次执行同样的查询会先从缓存中查询，如果缓存中没有才会从数据库中查询，
        并缓存到这个HashMap中，当添加修改删除这些操作执行时会清空一级缓存
    二级缓存：
        二级缓存是基于mapper的namespace的，是不同SqlSession可以共享的缓存，
        开启二级缓存后使用的是CachingExecutor，在查询的时候会从二级缓存中获取结果，
        二级缓存也是使用HashMap结构，一个mapper对应一个具体的map中的值，
        而这个对应的值底层又是一个map，这个map的key是当前mapper中的查询语句，
        存储的是具体语句的查询结果。
        二级缓存的设计非常灵活，它自己内部实现了一系列的Cache缓存实现类，
        并提供了各种缓存刷新策略如LRU，FIFO等等，
        还支持用户自定义实现Cache接口然后再mapper xml文件中指定<cache type="">指定自定义的Cache
        mybatis的二级缓存也有丰富的第三方库，比如mybatis-redis，就是使用第三方缓存数据库实现分布式的缓存
        
### 5、简述Mybatis的插件运行原理，以及如何编写一个插件？
    （1）mybatis的插件主要是对四大组件（Executor，StatementHandler，ParameterHandler，ResultSetHandler）进行代理扩展，
    mybatis在创建四大对象的时候，并不是直接返回的，而是通过interceptorChain.pluginAll(xx)来返回代理的对象。
    在pluginAll()方法中获取了所有的Interceptor进行代理，然后返回最终包装的对象
    在Interceptor中可以对四大组件的各种方法进行拦截做相应的处理，如分页等
    （2）编写插件流程：
        需要实现Integerceptor接口，然后通过注解@Intercepts注解，指定一个或多个@Signature注解
        每个@Signature注解声明了拦截四大组件中哪个组件以及组件具体的方法和参数，
        实现Interceptor接口的intercept方法，改方法为拦截的方法，具体的增强逻辑在此编写
        实现Interceptor接口的plugin方法，创建目标对象的代理对象
        实现Interceptor接口的setProperties方法，设置拦截器属性
        
        另外还要再配置文件中的<plugins>标签钟注册插件
    
    
## 二、编程题

### 请完善自定义持久层框架IPersistence，在现有代码基础上添加、修改及删除功能。【需要采用getMapper方式】
   已完善，可修改数据库配置后执行IPersistenceTest的测试方法
   