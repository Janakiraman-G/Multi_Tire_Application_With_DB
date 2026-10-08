apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata: 
    name: app-role
    namespace: webapps
rules:
    - apiGroups:
        - ""
        - apps
        - autoscaling
        - batch 
        - extensions
        - policy
        - rbac.authorization.k8.io