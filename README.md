# Introduction
This is a sample to show how to deploy with the canary strategy by ingress in kubernetes.
Ingress support two strategies, `canary by weight` and `canary by header`.
 
# Feature List
- test for canary by weight
- test for canary by header

# How to test
Here will use env to flag the different app version. As the definition in `app-1.0.0.yaml`:
```yaml
          env:
            - name: app.version
              value: 1.0.0
```
Please execute `mvn clean install` to build the image at first.

## canary by weight
The yaml files are in the `canary-weight` directory. 
First write a script as follow for test:
```bash
#! /bin/bash
v1=0;
v2=0;
count=100;
while [ $count -gt 0 ];
do
  result=$(curl -s "http://shf.boot.com/app-service/get/app/version")
  flag=$(echo $result | grep "1.0.0")
  if [ "$flag" != "" ]
  then 
	  v1=$(expr $v1 + 1)
  else 
	  v2=$(expr $v2 + 1)
  fi	  
  count=$(expr $count - 1)
done;
echo $v1;
echo $v2;
``` 

- Deploy the [app-1.0.0](./kubernetes/app-1.0.0.yaml), now 100% traffic to version 1.0.0:
```bash
$ kubectl apply -f ./kubernetes/0namespace.yaml -f ./kubernetes/app-1.0.0.yaml -f ./kubernetes/ingress-1.0.0.yaml
```

- Execute the script, the result as follow： 
```text
100
0
```
We will always get **1.0.0**.

- Deploy the version 2.0.0 by [app-2.0.0](./kubernetes/app-2.0.0.yaml)
```bash
$ kubectl apply -f ./kubernetes/app-2.0.0.yaml 
```

- Next deploy the [ingress-canary-2.0.0](./kubernetes/canary-weight/ingress-canary-2.0.0.yaml) 
to proxy the app-2.0.0, set 20% traffic to version 2.0.0:
```bash
$ kubectl apply -f ./kubernetes/canary-weight/ingress-canary-2.0.0.yaml
```

- Execute the script, the result as follow：
```text
81
19
```
The output results are basically consistent with the expected preset ratio(**<font color="blue">20%</font>**).

- Until everything goes well, delete `ingress-canary` and deploy [ingress-2.0.0](./kubernetes/canary-weight/ingress-2.0.0.yaml) to set 100% traffic to version 2.0.0
```bash
$ kubectl delete -f ./kubernetes/canary-weight/ingress-canary-2.0.0.yaml
$ kubectl apply -f ./kubernetes/canary-weight/ingress-2.0.0.yaml
```

- Execute the script, the result as follow： 
```text
0
100
```
We will always get **2.0.0**.

- Cleanup
```bash
$ kubectl delete ns canary-deploy
```

## canary by header
The yaml files are in the `canary-weight` directory. 
Write the test script as follow:
```bash
#! /bin/bash
echo $(curl -s -H "TAG: a" "http://shf.boot.com/app-service/get/app/version")
echo $(curl -s -H "TAG: b" "http://shf.boot.com/app-service/get/app/version")
echo $(curl -s -H "TAG: c" "http://shf.boot.com/app-service/get/app/version")
echo $(curl -s "http://shf.boot.com/app-service/get/app/version")
echo $(curl -s -H "TAG: d" "http://shf.boot.com/app-service/get/app/version")
echo $(curl -s -H "TAG: b" "http://shf.boot.com/app-service/get/app/version")
```

- Deploy the [app-1.0.0](./kubernetes/app-1.0.0.yaml), now 100% traffic to version 1.0.0:
```bash
$ kubectl apply -f ./kubernetes/0namespace.yaml -f ./kubernetes/app-1.0.0.yaml -f ./kubernetes/ingress-1.0.0.yaml
```

- Execute the script, the result as follow： 
```text
Current version is : 1.0.0
Current version is : 1.0.0
Current version is : 1.0.0
Current version is : 1.0.0
Current version is : 1.0.0
Current version is : 1.0.0
```
We will always get **1.0.0**.

- Deploy the version 2.0.0 by [app-2.0.0](./kubernetes/app-2.0.0.yaml)
```bash
$ kubectl apply -f ./kubernetes/app-2.0.0.yaml 
```

- Next deploy the [ingress-header-b](./kubernetes/canary-header/ingress-header-b.yaml) 
to proxy the app-2.0.0. Then when the request include a header which is [TAG: b] will be oriented to version 2.0.0:
```bash
$ kubectl apply -f ./kubernetes/canary-header/ingress-header-b.yaml
```

- Re-run the script, the result as follow： 
```text
Current version is : 1.0.0
Current version is : 2.0.0
Current version is : 1.0.0
Current version is : 1.0.0
Current version is : 1.0.0
Current version is : 2.0.0
```
As expected, the output of the second and sixth contain `2.0.0`. Others contain `1.0.0`.

- Until everything goes well, delete `ingress-header-b` and deploy [ingress-2.0.0](./kubernetes/canary-weight/ingress-2.0.0.yaml) to set 100% traffic to version 2.0.0
```bash
$ kubectl apply -f ./kubernetes/canary-header/ingress-2.0.0.yaml
$ kubectl delete -f ./kubernetes/canary-header/ingress-header-b.yaml
```

- Re-run the script, the result as follow： 
```text
Current version is : 2.0.0
Current version is : 2.0.0
Current version is : 2.0.0
Current version is : 2.0.0
Current version is : 2.0.0
Current version is : 2.0.0
```
We will always get **2.0.0**.

- Cleanup
```bash
$ kubectl delete ns canary-deploy
```

# Reference
- https://github.com/kubernetes/ingress-nginx/blob/master/docs/user-guide/nginx-configuration/annotations.md#canary