# AWS Zero to Hero — Part 10 (Chapters 25–26)
## English Study Notes + Original Transcript Preservation

> **Coverage:** Chapter 25 — AWS ECS (Elastic Container Service); Chapter 26 — AWS EKS (Elastic Kubernetes Service)  
> **Source policy:** Appendix A preserves the supplied transcript in its original order, with timestamps, speech-recognition wording, and repetitions. Study notes and assistant-added clarifications are kept in separate sections.

## Contents
1. Chapter 25 — AWS ECS
2. Chapter 26 — AWS EKS
3. ECS vs EKS
4. End-to-End Diagrams
5. Common Gotchas
6. Quick Revision Q&A
7. No-Skip Checklist
8. Board-Level Answer
9. Additional Points Added
10. Appendix A — Original Source, Preserved

---

# 1. Chapter 25 — AWS ECS (Elastic Container Service)

## 1.1 What is ECS?
The transcript defines ECS as a cloud-based container management service. It is used to run and manage Docker containers on AWS. ECS simplifies deployment and management of containerized applications, reducing the need to configure and monitor every server manually.

The transcript uses Docker as the default/common example and also mentions support for private registries.

## 1.2 Why use ECS?
The key benefits described in the transcript are:
- Focus on container-based applications.
- Help automate application/container deployment and management.
- Help manage multiple task/replica instances of an application.
- Provide capabilities to configure/manage scaling and load balancing at the service level.
- Reduce operational work compared with traditional manual setup.

> Cost note: the transcript explicitly warns that ECS is not a free service and test resources may incur charges. Actual cost depends on resource type, compute, storage, networking, Region, and usage.

## 1.3 Core ECS terms

| Term | Meaning | Memory shortcut |
|---|---|---|
| Cluster | A logical environment/group where ECS tasks and services run and are managed | Environment |
| Task Definition | A blueprint/configuration describing the container image and settings | Recipe |
| Task | A running container workload launched from a task definition | Running workload |
| Service | A capability that helps maintain the desired number of tasks and manage deployment/scaling | Manager |
| Container image | A packaged image containing the application and its dependencies | Package |
| Registry | A place to store container images, such as Docker Hub or a private registry | Image store |

In the transcript's simple mental model, the cluster represents the environment/resources, the task definition describes container configuration, the task is the running workload, and the service manages task count, availability, and scaling.

## 1.4 What is configured in a Task Definition?
The transcript compares this to options passed to Docker `run`. A task definition describes the container image and runtime configuration, such as:
- Container image/name
- CPU and memory requirements
- Environment variables
- Port mappings
- Storage/volume configuration
- Container- and task-related settings

Exact fields vary depending on launch type and task-definition settings.

## 1.5 Conceptual ECS architecture

```text
AWS ECS
└── Cluster
    ├── Task Definition (blueprint)
    ├── Service (desired count / deployment / scaling)
    └── Tasks (running container workloads)
          └── Container image from a registry
```

### Example workflow
1. Write the application code.
2. Build a Docker image.
3. Push the image to a registry.
4. Create an ECS Task Definition.
5. Configure an ECS cluster.
6. Create a service or run a task.
7. Verify application health, logs, scaling, and cost.

## 1.6 Console walkthrough — transcript flow
The transcript starts a walkthrough by opening ECS/Elastic Container Service in the AWS Console. If the dashboard is not visible, it mentions opening ECS through the Services menu or search. The next step is to give the cluster a name.

Practical checklist:
- Open the ECS service.
- Choose the option to create a cluster.
- Set the cluster name.
- Choose compute/launch configuration according to requirements.
- Verify task-definition and container-image settings.
- Run a task or service and test the application.
- Clean up resources when they are no longer needed.

**Important:** Console UI and available options can change. Verify the currently available launch/compute choices against the project's requirements.

## 1.7 Scaling and load balancing in ECS
The transcript connects the ECS service with scaling and load balancing for container tasks.

Conceptual sequence:
```text
Client traffic
    ↓
Load balancer (if configured)
    ↓
ECS Service
    ↓
Desired number of Tasks
    ↓
Containerized application
```

- A service helps maintain the desired task count.
- If a load balancer is configured, it can distribute traffic to healthy targets.
- Scaling policies and health checks must be configured correctly; merely creating an ECS cluster is not a complete production setup.

## 1.8 ECS cost and cleanup
The transcript specifically cautions against assuming ECS is free. For a learning lab:
1. Check whether tasks and services need to be stopped/deleted.
2. If EC2 capacity is used, inspect related instances/resources.
3. Verify load balancers, public IPs, volumes, logs, and networking resources.
4. Review ECR or other registry images according to retention needs.
5. Check the billing dashboard/Cost Explorer for unexpected costs.

---

# 2. Chapter 26 — AWS EKS (Elastic Kubernetes Service)

## 2.1 What is Kubernetes?
The transcript introduces Kubernetes as an open-source platform that automates deployment, scaling, and management of containerized applications.

Building a Docker image is only one part of deployment. Production workloads also involve replicas, load balancing, scaling, availability, recovery, and management. Kubernetes is used to orchestrate these concerns.

## 2.2 What does EKS do?
Amazon EKS is AWS's managed Kubernetes service. The transcript's key point is that EKS helps manage the Kubernetes control plane; application workloads run on worker nodes.

The transcript uses the term “master node”; modern Kubernetes documentation generally prefers **control plane**.

## 2.3 Two major components of a Kubernetes cluster

| Component | Role |
|---|---|
| Control plane (called master in the transcript) | Coordinates cluster state, scheduling/orchestration, and cluster management |
| Worker nodes | Run application Pods/containers |

Simplified diagram:
```text
Kubernetes / EKS Cluster
┌─────────────────────────────────────┐
│ Control plane — managed by AWS EKS  │
└──────────────────┬──────────────────┘
                   │ coordinates
       ┌───────────┴───────────┐
       ▼                       ▼
  Worker node 1            Worker node 2
  App Pods                 App Pods
```

The transcript describes the control plane as the cluster's “brain.” With EKS, AWS manages the control-plane infrastructure and helps make the control plane resilient across multiple Availability Zones.

## 2.4 Worker compute options: EC2 and Fargate
The transcript mentions two options for worker workloads:

### Option A — EC2 worker nodes
- Use EC2-based worker capacity.
- Plan/manage instance types, compute capacity, scaling, and node configuration.
- Can provide more infrastructure-level control.

### Option B — AWS Fargate
- A serverless compute option that reduces the need to manage underlying server instances directly.
- Provides compute according to the supported workload model and configuration.
- Can reduce node-management overhead; cost depends on workload and configuration.

> Availability and limitations of EC2/Fargate options depend on the EKS configuration and supported workload options.

## 2.5 VPC, public/private subnets, and networking
The transcript explains EKS architecture in the context of a VPC:
- A public subnet may be useful for an internet-facing entry point/load balancer when public access is required.
- Keeping worker nodes in private subnets is a common design choice.
- Components such as an Internet Gateway, NAT, and subnet routing may be part of the connectivity design.
- Public/private placement should be decided based on the application's exposure and security requirements.

Conceptual network:
```text
Internet
   |
Internet Gateway
   |
Public subnet: load balancer / public entry (if needed)
   |
Private subnet(s): worker nodes / application workloads
   |
Required outbound access via NAT or approved endpoints
```

This is a conceptual example; every EKS cluster is not required to use exactly this topology.

## 2.6 High availability
The transcript says that the EKS control plane operates with replicas across multiple Availability Zones, reducing the risk of a single point of failure. Application-side high availability also requires appropriate design of worker capacity, replicas, networking, health checks, and failure-domain placement.

**Important distinction:** a managed control plane does not automatically make the application highly available. Application Pods and worker capacity must also be distributed/configured appropriately.

## 2.7 High-level EKS setup flow
The transcript explains concepts before the practical setup. For study purposes, the high-level flow is:

1. Plan VPC/subnet/networking requirements.
2. Create the EKS cluster/control plane.
3. Choose worker compute — EC2 nodes or a supported Fargate setup.
4. Configure IAM roles/access and cluster permissions.
5. Deploy workloads using application manifests/Helm charts.
6. Configure Service/Ingress/load balancer and network access.
7. Verify health, scaling, logs, security, and costs.
8. Clean up billable resources when the lab is finished.

This is an organized conceptual flow; the supplied transcript may not cover detailed CLI implementation for every step.

## 2.8 Conceptual difference between EKS and ECS
- **ECS:** AWS-native container orchestration service; cluster, task definition, task, and service are central ECS concepts.
- **EKS:** managed Kubernetes service; uses Kubernetes concepts such as cluster, control plane, worker nodes, Pods, and Kubernetes manifests.
- ECS may be a good fit when AWS-native container orchestration and ECS abstractions meet the requirements.
- EKS may be a good fit when the Kubernetes ecosystem, Kubernetes APIs/tooling, or existing Kubernetes skills/workloads are needed.

Neither option is universally best for every use case. Compare team skills, portability, operational complexity, integrations, cost, and application requirements.

---

# 3. ECS vs EKS

| Dimension | ECS | EKS |
|---|---|---|
| Main abstraction | Task Definition, Task, Service, Cluster | Kubernetes cluster, Pods, Deployments, Services, manifests |
| Orchestration model | AWS-native ECS model | Kubernetes |
| Control-plane management | ECS service handles orchestration | AWS manages the EKS control plane |
| Workload compute | EC2/Fargate options according to configuration | EC2/Fargate options according to configuration |
| Learning curve | More direct once ECS concepts are understood | Requires learning Kubernetes concepts and ecosystem |
| Common fit | AWS-centric container workloads | Kubernetes-based workloads and ecosystem/tooling needs |
| Decision factors | Simplicity, AWS integration, operational model | Kubernetes compatibility, portability needs, team expertise |

---

# 4. End-to-End Diagrams

## 4.1 ECS deployment
```text
Application code
      ↓
Build container image
      ↓
Push image to registry
      ↓
ECS Task Definition
      ↓
ECS Cluster + Service
      ↓
Running Tasks / containers
      ↓
Health checks, scaling, logs, cost monitoring
```

## 4.2 EKS deployment
```text
Application code + container image
      ↓
Kubernetes manifests / Helm chart
      ↓
EKS Cluster
      ├── AWS-managed Control Plane
      └── Worker compute (EC2 or supported Fargate)
                ↓
             Pods
                ↓
       Service / Ingress / traffic routing
```

## 4.3 Quick service map

| Need | Relevant concept |
|---|---|
| Define image and container runtime settings in ECS | Task Definition |
| Run a container workload in ECS | Task |
| Maintain desired task count and manage deployments | ECS Service |
| Group ECS resources/workloads | ECS Cluster |
| Kubernetes cluster orchestration/brain | Control plane |
| Run application workloads in Kubernetes | Worker nodes and Pods |
| Reduce direct worker-server management | Fargate, where supported |
| Network isolation and subnet placement | VPC, public/private subnets, routes |

---

# 5. Common Gotchas

1. **ECS is not the same as Docker:** Docker builds/runs containers; ECS orchestrates/manages container workloads on AWS.
2. **Task Definition vs Task:** the definition is the blueprint; a task is the running workload.
3. **Cluster vs Service:** the cluster is the logical environment; a service helps maintain/manage desired tasks.
4. **ECS can incur charges:** do not assume a learning cluster is free.
5. **Creating a cluster is not the whole deployment:** image, task definition, compute, networking, IAM, and service configuration also matter.
6. **Kubernetes control plane vs worker nodes:** EKS manages the control plane; application workloads run on worker compute.
7. **EKS does not automatically make the app highly available:** replica placement, worker capacity, and health checks matter.
8. **A public subnet does not mean every worker must be public:** private worker subnets are a common security design.
9. **EC2 vs Fargate is a trade-off:** compare infrastructure control/management overhead and actual workload costs.
10. **ECS vs EKS is a contextual choice:** do not claim that one is always better.
11. **Cleanup must be comprehensive:** inspect compute, load balancers, networking, storage, logs, and other billable resources.
12. **Terminology evolves:** the transcript uses “master”; “control plane” is the preferred modern term.

---

# 6. Quick Revision Q&A

**Q1. What is ECS?**  
An AWS cloud-based container management/orchestration service for running and managing containerized applications.

**Q2. What is an ECS Cluster?**  
A logical environment used to organize and run ECS services and tasks.

**Q3. What is a Task Definition?**  
A blueprint describing the container image and runtime configuration.

**Q4. What is an ECS Task?**  
A running workload launched using a task definition.

**Q5. What does an ECS Service do?**  
It helps maintain the desired number of tasks and manage deployment/scaling; load balancing can be configured where required.

**Q6. Is ECS necessarily free?**  
No. The transcript explicitly warns that charges may apply.

**Q7. What is Kubernetes?**  
An open-source container orchestration platform for automating deployment, scaling, and management.

**Q8. What is EKS?**  
AWS's managed Kubernetes service; AWS manages the EKS control plane.

**Q9. Where do application workloads run in Kubernetes?**  
On worker compute, as Pods/containers.

**Q10. What worker compute options are mentioned in the transcript?**  
EC2 instances and Fargate.

**Q11. Does EKS automatically make every application highly available?**  
No. Workload replicas, worker placement, health checks, and networking also need to be designed.

**Q12. When might ECS be preferable to EKS?**  
When the AWS-native ECS model meets the requirements without a need for Kubernetes-specific APIs or ecosystem features.

**Q13. When might EKS be preferable?**  
When Kubernetes compatibility, ecosystem tooling, or existing Kubernetes workloads/skills are important.

---

# 7. No-Skip Checklist

- [ ] ECS definition as a cloud-based container management service
- [ ] Docker containers and private registry mention
- [ ] ECS benefits and reduced manual server work
- [ ] Cluster, Service, and Task terms
- [ ] Task Definition vs running Task
- [ ] Image, environment variables, ports, storage/volumes, and resource configuration
- [ ] ECS console/service navigation
- [ ] Creating and naming a cluster
- [ ] Cost warning and learning-lab cleanup
- [ ] Service scaling and load-balancing concept
- [ ] Kubernetes definition and orchestration goals
- [ ] Control plane/master and worker nodes
- [ ] EKS management of the control plane
- [ ] Availability Zones and control-plane resilience
- [ ] EC2 and Fargate worker compute options
- [ ] VPC, public/private subnets, NAT, and Internet Gateway
- [ ] Public entry vs private worker placement concept
- [ ] ECS vs EKS comparison
- [ ] Cost/security/availability gotchas
- [ ] Original transcript in Appendix A

---

# 8. Board-Level Answer

**Amazon ECS** is an AWS container orchestration service used to run and manage containerized applications. Its key concepts are a cluster, task definition, task, and service. The task definition specifies the image and runtime configuration; a task is the running workload; and a service helps maintain the desired task count and manage deployment/scaling. ECS can reduce manual container-management work, but compute, networking, and related resources may incur charges.

**Amazon EKS** is AWS's managed Kubernetes service. Kubernetes automates deployment, scaling, and management of containerized applications. An EKS cluster has an AWS-managed control plane and worker compute where application Pods run. Worker compute can use EC2 or supported Fargate configurations. Networking, IAM, workload replicas, health checks, scaling, and cost controls still require appropriate design. ECS and EKS should be selected according to application requirements, Kubernetes needs, team expertise, operational complexity, and cost.

---

# 9. Additional Points Added

> **These are assistant-added clarifications, not verbatim parts of the original transcript.**

- A container image and a running container are different concepts: an image is a packaged template; a container is a running instance/workload created from it.
- Think of an ECS Task Definition as a reusable deployment recipe and a Task as a running execution of that recipe.
- In Kubernetes, a Pod is a common deployable unit; a Pod can contain one or more tightly related containers.
- Even with a managed EKS control plane, application deployment, IAM, networking, node capacity, observability, and security still require attention.
- For production, use appropriate secrets-management patterns instead of hard-coding secrets in source code or plain environment variables.
- Cost reviews should consider load balancers, data transfer, storage, logging, and NAT/network resources in addition to compute.
- AWS console/CLI behavior can change; verify the installed tooling and official service documentation for live implementations.

---

# 10. Appendix A — Original Source, Preserved

The supplied transcript for Chapters 25 and 26 is included below in its original order. Timestamps, speech-recognition spellings, wording, and repetitions have intentionally not been normalized or corrected.

```text
Chapter 25: AWS ECS (Elastic Container Service)
8:31:018 hours, 31 minutes, 1 secondबात करने वाले हैं
8:31:268 hours, 31 minutes, 26 secondsचलिए आगे बढ़ते हैं सबसे पहले एक छोटी सी डेफिनेशन देख लेते हैं सो ईएस क्या होता है इट इज अ क्लाउड बेस्ड कंटेनर मैनेजमेंट सर्विस क्लाउड बेस्ड क्यों एज वी आर
8:31:348 hours, 31 minutes, 34 secondsवर्किंग विद द amazon-in मैनेजमेंट सर्विस मतलब अगर आपने
8:31:438 hours, 31 minutes, 43 secondsडिसाइड किया है कि आप अपनी एप्लीकेशन को कंटेनर के फॉर्म में लाइक डॉकर को यूज करके ठीक है
8:31:508 hours, 31 minutes, 50 secondsडिप्लॉयड करना चाहते हो मैनेज करना चाहते हो अगर आप डिसाइड करते हो कि आपको क्लाउ में ही डिप्लॉयड ऑप्शन एंड उसी के बारे में हम
8:31:598 hours, 31 minutes, 59 secondsलोग बात करेंगे एंड इट अलाउ यू टू रन एंड मैनेज डॉकर कंटेनर्स ऑन अ क्लस्टर ऑफ वर्चुअल सर्वर्स बट यह डॉकर के लिए
8:32:078 hours, 32 minutes, 7 secondsलिमिटेड नहीं है आप अपनी प्राइवेट रजिस्ट्री वगैरह भी ये सपोर्टेड करता है सो इट्स कंपलीटली फाइन बट बाय डिफॉल्ट डॉकर में सपोर्टेड है सो जो सबसे वाइड
8:32:178 hours, 32 minutes, 17 secondsयूज्ड जो डॉकर रजिस्ट्री एंड ऑल सो इट्स गुड चलिए अब बात करते हैं कि ईसीएस ही
8:32:248 hours, 32 minutes, 24 secondsक्यों कि क्योंकि यह देखो ये कंटेनर बेस्ड है और फोकस्ड है कंटेनर बेस्ड एप्लीकेशंस
8:32:318 hours, 32 minutes, 31 secondsके लिए ओके एंड क्या खासियत होती है ईसीएस की उसके बारे में बात करें तो इट ऑटोमेटिक हैंडल्स क्रिएशन मैनेजमेंट एंड अपडेटिंग
8:32:398 hours, 32 minutes, 39 secondsऑफ योर एप्लीकेशन लेट्स से कि आपने एक एप्लीकेशन को डेवलप किया बनाया ओके एंड डॉकर की मदद से आपने उसको कंटेनराइज कर
8:32:468 hours, 32 minutes, 46 secondsदिया उसकी इमेज बना दी एंड डॉकर रजिस्ट्री में डॉकर हब में अपलोड कर दिया एंड एवरीथिंग सो आपकी एप्लीकेशन डि डिप्लॉयड
8:32:548 hours, 32 minutes, 54 secondsके लिए रेडी है ओके एंड आपको ज्यादा हेडेक नहीं चाहिए मैनुअल वर्क नहीं चाहिए लाइक
8:33:018 hours, 33 minutes, 1 secondजो हम इजी टू में करते थे खुद का सर्वा सेटअप करना डॉका सेटअप करो फिर एप्लीकेशन को रन करो फिर उसको मॉनिटर करो कोई
8:33:098 hours, 33 minutes, 9 secondsप्रॉब्लम तो नहीं है कुछ नहीं है ठीक है सो काफी मैनुअल वर्क था हेडेक भी थी बट इसस में वो सब चीजें नहीं है आप अपना इमेज
8:33:168 hours, 33 minutes, 16 secondsका नाम दे दो बाकी सब होती है एंड सो ऑन सो ये इसकी खासियत है
8:33:288 hours, 33 minutes, 28 secondsकि देखो आपने डिसाइड कर लि ईसीएस को यूज करने के लिए अब आपका नेक्स्ट स्टेप क्या है ठीक है तो ईसीएस के थोड़े से टर्म्स हम लोग समझ लेते हैं सो दैट कि जब हम
8:33:368 hours, 33 minutes, 36 secondsडायरेक्टली यह सर्विस यूज करें तो हमें थोड़ा सा हमें अवेयरनेस हो या तो हमें पता हो कि हम कर क्या रहे ठीक है तो इसमें बेसिकली तीन टर्म्स है जो आपको ध्यान रखनी
8:33:458 hours, 33 minutes, 45 secondsहै पहली हो गई क्लस्टर तो क्लस्टर क्या होता है ग्रुप ऑफ टास्क एंड सर्विसेस एंड इट होस्ट ऑल द रिसोर्सेस एंड इंफ्रास्ट्रक्चर
8:33:538 hours, 33 minutes, 53 secondsओके एंड इसके बाद है सर्विस हैंडल्स स्केलेबिलिटी एंड लोड बैलेंसिंग ऑफ द कंटेनर टास्क है रिप्रेजेंट द रनिंग
8:34:018 hours, 34 minutes, 1 secondकंटेनर ऑफ योर एप्लीकेशन सो यह बहुत ही बेसिक चीजें हैं जो आपको एप्लीकेशन को रन करने के लिए चाहिए जैसे कि कोई तो इंफ्रास्ट्रक्चर होगा चाहे वर्चुअली है
8:34:098 hours, 34 minutes, 9 secondsऑफकोर्स क्लाउड की हम लोग बात कर रहे हैं तो वर्चुअल एनवायरमेंट भी है लेट्स से वो भी
8:34:258 hours, 34 minutes, 25 secondsयूज होगा कुछ तो रिसोर्सेस होंगे मेमोरी होगी रम होगा सीपीयू होगा राइट वो किसके अंडर रहता है क्लस्टर के अंदर ठीक है फिर
8:34:348 hours, 34 minutes, 34 secondsआही बात सर्विस क्यों क्योंकि देखो आपकी एप्लीकेशन जब रन हो रही है उसको मैनेज करना वह काम किसका है सर्विस का है
8:34:438 hours, 34 minutes, 43 secondsक्योंकि क्लस्टर तो भाई रिसोर्सेस और ये सब चीजें देखता है ओके इसके बास सर्विस होती है जो सर्विस क्या है आपकी कंटेनर या
8:34:508 hours, 34 minutes, 50 secondsतो आपकी एप्लीकेशन को देखेगा कि भाई इसको स्केल स्केल करना है क्या रिसोर्स एलोकेशन करना है क्या कितने रेप्स चलाने हैं ठीक
8:34:588 hours, 34 minutes, 58 secondsहै मल्टीपल इंस्टेंसस तो नहीं चलाने बंद हो गया तो क्या करना है ठीक है सो मेनी थिंग्स राइट सो वो सर्विस करता है उसके
8:35:058 hours, 35 minutes, 5 secondsबाद टास्क है टास्क मतलब सिंपल है आपका जो कंटेनर इंस्टेंस चल रहा है ना ईसीएस में वो टास्क हो गया इसके अलावा एक होती है
8:35:138 hours, 35 minutes, 13 secondsटास्क की डेफिनेशन जहां पे आप बताते हो कि भाई कंटेनर तो रन हो रहा है इसके अंदर कौन सी इमेज यूज करनी है कौन सा एनवायरमेंट
8:35:218 hours, 35 minutes, 21 secondsवेरिएबल यूज करना है ठीक है जैसे कि जो हम लोग डॉकर में करते थे मतलब जैसे डॉकर रन कर करते थे ना काफी सारे ऑप्शन देते थे जैसे स्टोरेज हो गया वॉल्यूम हो गया
8:35:298 hours, 35 minutes, 29 secondsएनवायरमेंट वेरिएबल हो ग पोर्ट बाइंडिंग हो गई ये सारी चीजें आ टास्क के डेफिनेशन में देते हू सिंपल सी इतनी सी चीजें हैं
8:35:378 hours, 35 minutes, 37 secondsइतने से टर्म्स आपने ठीक है याद रख लिए समझ लिए ना
8:35:478 hours, 35 minutes, 47 secondsदैट्ची जें हम लोग ने पढ़ी कैसे दिखती है आप उसको विजुलाइज कैसे कर सकते हो इस तरह से देखो ये ईसीएस हो गया ईसीएस के अंदर
8:35:558 hours, 35 minutes, 55 secondsक्लस्टर सो आपने एक क्लस्टर बना दिया ठीक है एक क्लस्टर हो गया ये देखो ये वाला बॉक्स अब ये बॉक्स के अंदर आपने डिसाइज
8:36:038 hours, 36 minutes, 3 secondsकिया कि भाई आपको एप्लीकेशन चलानी सिंपल है टास्क डेफिनेशन यहां दिख रहा है कि आपको कौन सी इमेज या तो कौन सा डॉकर इमेज
8:36:118 hours, 36 minutes, 11 secondsको जैसे कि रन करना वो आपने बता दिया एंड वो सर्विस है बाकी सब हैंडल कर लेगा वेरी वेरी सिंपल अभी हम लोग डायरेक्टली चलते
8:36:198 hours, 36 minutes, 19 secondsहैं एन वेब सर्वर के ऑफिशियल वेब पेज पे और ये चीजें करके देखेंगे सो ये आ गए हम इसके ऑफिशियल वेबसाइट पे यह एक्चुअली मेरा
8:36:278 hours, 36 minutes, 27 secondsडैशबोर्ड है aws.com यहां पे मैंने रजिस्टर कर लिया है e2 इंस्टेंस वाले वीडियो में मैंने बताया था कि रजिस्टर
8:36:358 hours, 36 minutes, 35 secondsकैसे करना है आपको क्रेडिट कार्ड डिटेल्स प्रोवाइड करनी पड़ेंगी बिकॉज अ यह फ्री ऑफ फ्री सर्विस नहीं है हां यह ऑफकोर्स मैं
8:36:428 hours, 36 minutes, 42 secondsथोड़ा सा नोट करवा दूं आपको वार्निंग के साथ यह फ्री सर्विस नहीं है ईसीएस ओके लाइक द फ्री टियर हम लोग ने देखा था e2
8:36:508 hours, 36 minutes, 50 secondsमें वो फ्री टियर था इसमें पैसे लगेंगे बहुत ज्यादा नहीं लगते हैं मैं मैं दो-तीन दिन से टेस्टिंग कर रहा हूं यूज कर रहा हूं अब जाके भी
8:36:598 hours, 36 minutes, 59 seconds01 01 मुझे अभी तक चार्ज हुए हैं सो टेस्टिंग परपस के लिए आप ठीक है जा सकते हो थोड़ा
8:37:078 hours, 37 minutes, 7 secondsबहुत ना के बराबर अ पैसा लगता है ठीक है छोटी एप्लीकेशन होनी चाहिए ऑफकोर्स बहुत बड़ी होगी
8:37:148 hours, 37 minutes, 14 secondsरिसोर्सेस ज्यादा लगेंगे हो सकता है चार्ज ज्यादा हो फिर भी बहुत ज्यादा नहीं हो आई आई आई डोंट थिंक सो 100 200 उससे ज्यादा तो जाएगा नहीं तो हम लोग लर्निंग के लिए
8:37:238 hours, 37 minutes, 23 secondsतो कर ही सकते हैं यार क्योंकि हम लोग इतने तो कोर्सेस में दे देते हैं ठीक है एंड सिंपल आपको क्या करना है टेस्टिंग कर लो देख लो चेक कर लो कैसे चलती है चीजें
8:37:318 hours, 37 minutes, 31 secondsउसके बाद अपनी सब कुछ हम लोग डिलीट भी कर देंगे चलो शुरुआत करते हैं सबसे पहले क्लस्टर बनाने से देखो ओके तो अगर आपको ये वाला डैशबोर्ड नहीं दिख रहा आप क्या करो
8:37:398 hours, 37 minutes, 39 secondsया तो सर्विसेस में चले जाओ तो आपको यहां पे इलास्टिक कंटेनर सर्विस इस तरह से दिखेगी या तो आप सर्च कर लेना यहां पे लेट्स से आपने सर्च किया ला इलास्टिक
8:37:488 hours, 37 minutes, 48 secondsकंटेनर सर्विस इस पर मैंने क्लिक किया तो आपको यही वाला ठीक है यह पेज दिखेगा इसके बाद सिंपली हम लोग न्यू
8:37:568 hours, 37 minutes, 56 secondsक्लस्टर करेंगे इसमें आपको नाम दे देना है जैसे कि डेफ क्लस्टर यहां पे दिया है सो लेट्स से कि हम लोग यहां पे दे देंगे माय
8:38:068 hours, 38 minutes, 6 secondsक्लस्टर ठीक है सो मैं इस तरह से नाम दे देता हूं माय क्लस्टर यहां पे नेम स्पेस हो गया इसके
8:38:138 hours, 38 minutes, 13 secondsबाद ये पूछता है इंफ्रास्ट्रक्चर अब यहां भी आपको कुछ अलग-अलग ऑप्शंस मिलते हैं जैसे कि देखो अ आपको अपनी एप्लीकेशन किस
8:38:218 hours, 38 minutes, 21 secondsइंफ्रास्ट्रक्चर में या तो किस टाइप में रन करना है यहां पे एक फार गट जो कि होता
8:38:288 hours, 38 minutes, 28 secondsहै सर्वरलेस सर्वरलेस मतलब आप वह सर्वर को ठीक है मैनुअली या तो फिजिकली उसको आप ठीक
8:38:368 hours, 38 minutes, 36 secondsहै मेंटेन नहीं करोगे हैंडल नहीं करोगे ठीक है एंड इजी टू इंस्टेंस ठीक है जहां पे आप मैनुअल कॉन्फिन करना चाहते हो उन
8:38:458 hours, 38 minutes, 45 secondsलोगों के लिए टू इंस्टेंस इज वेरी हेल्पफुल यहां पे देखो डिस्क्रिप्शन प दिया है मैनुअल कॉन्फिन जैसे कि आपका लार्ज वर्कलोड्स है आप मैनुअल ठीक है उसको
8:38:548 hours, 38 minutes, 54 secondsहैंडल करना चा हो उसमें चेंजेज करना चाहते हो तो इजी टू इंस्टेंस भी इट्स गुड फॉर यू ठीक है बट हम लोग यहां पे फार गेट चलते हैं बिकॉज हम लोग की सिंपल सी एप्लीकेशन
8:39:028 hours, 39 minutes, 2 secondsहै हमें नहीं चाहिए एक अपना खुद का ी टू इंस्ट ठीक है इंफ्रास्ट्रक्चर ए इसके बाद सिंपल कर लो क्रिएट सो यहां पे थोड़ी ही
8:39:088 hours, 39 minutes, 8 secondsदेर में आपका क्लस्टर हो जाएगा रेडी ठीक है थोड़ी देर में आप इसको रिफ्रेश भी कर सकते हो यहां से सो यहां पे रिफ्रेश करने के बाद देखोगे यहां पे माय क्लस्टर आ गया
8:39:178 hours, 39 minutes, 17 secondsहै ओके इसके बाद जो आपका सेकंड स्टेप होगा वो होगा टास्क डेफिनेशन का ठीक है टास्क डेफिनेशन प चलते हैं क्यों क्योंकि जैसे
8:39:258 hours, 39 minutes, 25 secondsकि देखो अभी हम लोग ने बना दिया क्लस्टर और हम लोग ने बना दिया फार गट वगैरह ये सब इंफ्रास्ट्रक्चर व किस किस बेसिस पे रहेगा अभी हमको बताना है कि हमें कौन सी
8:39:348 hours, 39 minutes, 34 secondsएप्लीकेशन रन करना है वेरी सिंपल सो हम लोग क्या है क्रिएट न्यू टास्क की डेफिनेशन करेंगे लेट्स से यहां पे टास्क डेफिनेशन यहां पे आप नाम देना है माय अ
8:39:438 hours, 39 minutes, 43 secondsमाय टास्क इसके बाद यहां भी पूछ रहा है वो इंफ्रास्ट्रक्चर रिक्वायरमेंट किस कैसी रहेगी तो हम लोग फार गट ही यूज करते हैं
8:39:528 hours, 39 minutes, 52 secondsफार गट में भी आपको थोड़े से यहां पे ऑप्शंस मिल जाते हैं जैसे कि ओस या तो आर्किटेक्चर कौन सा यूज करना है मैं सिंपल
8:40:258 hours, 40 minutes, 25 secondsफॉर एग्जांपल एनेक्स सी मतलब जो ऑफिशियल जो एनेक्स एप्लीकेशन है ना हम लोग उसको ही रन करके
8:40:328 hours, 40 minutes, 32 secondsदेखेंगे एंड यहां पे आता है इमेज यू आरआईएमयू आरआईपीएल है लेट्स से आप किसी कंपनी वगैरह
8:40:408 hours, 40 minutes, 40 secondsमें काम कर रहे हो तो यहां पे प्राइवेट रजिस्ट्री पे इनेबल करके आप और इंफॉर्मेशन दे सकते हो लद यहां पे हमारी पब्लिक ही है
8:40:478 hours, 40 minutes, 47 secondsतो हम लोग सिंपली यहां पे नाम दे देंगे वेरी सिंपल एंड यहां पे आप कॉलन देके इस तरह से वर्जन भी दे सकते हो जैसे कि फॉर
8:40:548 hours, 40 minutes, 54 secondsएग्जांपल आई चूज टू रन द लेटेस्ट सिंपल ओके सो सिंपल आपको देखो एक कंटेनर का नाम
8:41:018 hours, 41 minutes, 1 secondदे देना है और यहां पर इमेज दे देनी कि कौन सी इमेज चलानी है यह डॉकर से बाय डिफॉल्ट यूज करेगा अदर वाइज किसी और
8:41:098 hours, 41 minutes, 9 secondsरजिस्ट्री पे आपका है तो आप पूरा फुल पाथ उसका दे सकते हो ठीक है इसके बाद आ जाते हैं पोर्ट मैपिंग पोर्ट मैपिंग क्या जरूरी
8:41:178 hours, 41 minutes, 17 secondsहै यस ऑफ कोर्स क्योंकि एंजने वेब सर्वर है तो उसमें मैंने 80 पोर्ट ही है ओके सो चलो पोर्ट 80 कर ले ता हूं
8:41:268 hours, 41 minutes, 26 secondsमैं उसके बाद सीपीयू वगर यहां पर आप रिसोर्स एलोकेशन लिमिट्स लिमिट सेट कर सकते हो कि भाई देखो स्केल जब कर रहे हैं
8:41:348 hours, 41 minutes, 34 secondsना एप्लीकेशन को स्केल तो करो लेकिन एक लिमिट के ज्यादा नहीं ठीक है सो यहां पे आप हार्ड लिमिट इस तरह से देख सकते हो सॉफ्ट लिमिट एंड ऑल कि आपको बहुत ज्यादा
8:41:438 hours, 41 minutes, 43 secondsबिल ना जाए एकदम से ठीक है सो यह चीजें भी प्रोवाइड करता है एडब्ल्यू ऐसा नहीं है कि अनलिमिटेड पैसा हो जाएगा एनवायरमेंट
8:41:508 hours, 41 minutes, 50 secondsवेरिएबल एप्लीकेबल है तो आप दे सकते हो लॉक कलेक्शन यहां पे बाय डिफॉल्ट इनेबल है हम लोग इनेबल रखते हैं बिकॉज कंटेनर्स एंड
8:41:588 hours, 41 minutes, 58 secondsएप्लीकेशंस के रिलेटेड जो भी लॉक्स है वो हमें दिख जाएंगे चलो हेल्थ चेक काफी सारे ऑप्शंस है यहां प आप देख सकते
8:42:068 hours, 42 minutes, 6 secondsहो स्टोरेज है एंड ऑल सिंपल है अभी हम लोग मैक्सिमम जो डिफॉल्ट कैटेगरी है उसके साथ ही जाते हैं यहां पर हम लोग क्रिएट कर
8:42:148 hours, 42 minutes, 14 secondsलेते हैं टास्क तो यहां प देखो टास्क डेफिनेशन हो गया ना क्रिएट तो यहां पर आपको यह ऑप्शन या तो आपको नेक्स्ट स्टेप
8:42:208 hours, 42 minutes, 20 secondsभी दिखाता है कि करोगे ना रन तभी तो होगा तो आप क्या करो क्रिएट सर्विस अपडेट सर्विस रन टास्क य
8:42:298 hours, 42 minutes, 29 secondsतीन तरीके हैं जिसको यूज करके आप अपने टास्क को या तो कंटेनर को रन कर सकते हो चलो मैं लेकिन इजी में दिखाता हूं पहले
8:42:368 hours, 42 minutes, 36 secondsक्लस्टर टास्क डेफिनेशन अभी मैं फिर से क्लस्टर पर जाता हूं तो सर्विस में हम लोग यहां पर सिंपल क्रिएट कर लेंगे ओके क्रिएट
8:42:458 hours, 42 minutes, 45 secondsसर्विस में आ गया हूं मैं लच टाइप पर आप क्लिक कर लो कंप्यूट ऑप्शन एंड फार गट ही है अपना ठीक है क्योंकि सर्वरलेस मोड हम लोग यूज कर रहे
8:42:538 hours, 42 minutes, 53 secondsहैं एस अ सर्विस देखो जो टास्क डेफिनेशन बनाई उसका यूज क्या है य देखो ये जो फैमिली है
8:43:018 hours, 43 minutes, 1 secondना यहां पर पूछेगा तो माय टास्क यहां पर ऑप्शन आ गया अगर बनाते नहीं तो आप कैसे मतलब क्या सेलेक्ट करते सर्विस का नाम तो
8:43:088 hours, 43 minutes, 8 secondsयहां पर मैं यहां पर सिंपल माय सर्विस इस तरह से नाम दे देता हूं रप्ल का ओके दिस इज वेरी इंटरेस्टिंग कि देखो आपको मतलब एक
8:43:188 hours, 43 minutes, 18 secondsइंस्टेंस पर रिला नहीं रहना है लेटस से कि आपकी जो वेबसाइट है इंपोर्टेंट है क्रिटिकल है कि कि इनके ऑफ एनी फेलियर एक
8:43:268 hours, 43 minutes, 26 secondsसेकंड इंस्टेंस चलते रहना चाहिए तो आप यहां पे मल्टीपल इंस्टेंस भी चला सकते हो लेट्स से यहां प मैं दो इंस्टेंस भी कर सकता हूं ठीक है लेट्स से चलो दो ही कर
8:43:348 hours, 43 minutes, 34 secondsलेते हैं लद वन इज गुड फॉर यू इसके बाद वेरी सिंपल आपको आ जाना है नेटवर्किंग पे ठीक है नेटवर्किंग पे आ गया
8:43:438 hours, 43 minutes, 43 secondsतो मैं सिंपल यहां पे क्रिएट न्यू सिक्योरिटी ग्रुप कर लेता हूं एंड यहां पे क्योंकि मुझे एनेक्स है जो वेब सर्वर उसको
8:43:518 hours, 43 minutes, 51 secondsएक्सेस करना है तो मैं सिंपली यहां पे एचटीटीपी दे देता हूं कि भाई एचटीटीपी को यूज करके मैं उसको एक्सेस करना चाहता हूं एंड सोर्स आप यहां पे कस्टम या फिर मैं
8:44:008 hours, 44 minutesएनी वेयर कर देता हूं फॉर नाउ कि हम लोग इजली उसको एक्सेस कर पाए सबसे इंपॉर्टेंट पब्लिक आईपी जो लास्ट में टर्न ऑन होना
8:44:078 hours, 44 minutes, 7 secondsचाहिए तभी आप इसको एक्सेस कर पाओगे पब्लिक के लिए एक आपको अवेलेबल जो यूआरएल है वो मिलेगा इसके बाद काफी ऑप्शंस है लोड
8:44:168 hours, 44 minutes, 16 secondsबैलेंसिंग वॉल्यूम टैक्स एक्सट्रा बट अभी हम लोग सेम सिंपल रख के क्रिएट करते हैं ओके सो रिफ्रेश करने पे मेरा जो सर्वेस है
8:44:258 hours, 44 minutes, 25 secondsयहां पे आ गया है एंड सर्विस के साथ-साथ ये ऑटोमेटिक जैसे ही आप सर्विस बनाते हो ना तो वो आपके जो टास्क है मतलब जो कंटेनर
8:44:318 hours, 44 minutes, 31 secondsहै वो खुद ही अपने आप रन कर देगा यह सबसे बेस्ट चीज होता है कि सब आपने बनाया नहीं कि आपका एप्लीकेशन रन अप एंड रनिंग मैंने
8:44:398 hours, 44 minutes, 39 secondsअभी रन नहीं किया कुछ नहीं किया ना अब आप देखोगे टास्क पे जाऊंगा मैं सो देखोगे अभी जो स्टेटस है वो क्या है पेंडिंग ओके अब
8:44:478 hours, 44 minutes, 47 secondsमैं इसको सिंपल रिफ्रेश करूंगा चेंज होगा ये देखो एक रनिंग में आ गया एंड उसके बाद दूसरा भी रनिंग में आ जाएगा अभी ये देखो मैंने क्या दो इंस्टेंस बोले थे कि भाई
8:44:568 hours, 44 minutes, 56 secondsमेरी एप्लीकेशन के दो अ मतलब दो इंस्टेंस साथ में पैरेलल रनिंग होना चाहिए तो ये देखोगे दोनों रनिंग हो गए ठीक है चलिए अभी
8:45:058 hours, 45 minutes, 5 secondsइसके बाद क्या करना है मैं आपको बताता हूं टास्क है यह तो एंड यह किसी भी एक पे आप कर लो क्योंकि आपकी ही एप्लीकेशन है चलो
8:45:138 hours, 45 minutes, 13 secondsमैं लेट्स से कि मैंने पहली वाली जो टास्क है उसपे रन किया आपको थोड़ी सी डिटेल दिख जाएगी जैसे कि कौन सा ये इमेज यूज़ कर रहा
8:45:208 hours, 45 minutes, 20 secondsहै एंड ऑल ओके लॉग कॉन्फ़िगरेशन अब जैसे कि मैंने यहां क्लिक किया तो आप यहां लॉक्स प क्लिक करोगे तो आपको जो कंटेनर्स
8:45:278 hours, 45 minutes, 27 secondsके लॉग है इस तरह से दिख जाएंगे ठीक है एंड कॉन्फिन में आते हैं एंड यहां पे नेटवर्क बाइंडिंग में चले जाऊ लद आप यहां
8:45:358 hours, 45 minutes, 35 secondsसे भी आपको दिख जाएगा कॉन्फिन तो कॉन्फिन में आप देखोगे यहां प पब्लिक आईपी दिख रही है राइट एंड सिंपली आप यहां पे ओपन एड्रेस
8:45:438 hours, 45 minutes, 43 secondsकरोगे ना सो आपको देखोगे वेलकम टू एजक्स ओके इट्स सो ब्यूटीफुल एंड सो इजी कि आपकी
8:45:508 hours, 45 minutes, 50 secondsजो वेबसाइट है अभी वो लाइव है आप ये जो आईपी है वो किसी को भी प्रोवाइड कर सकते हो शेयर कर सकते हो एंड व पर्सन यह
8:45:588 hours, 45 minutes, 58 secondsवेबसाइट को या तो लेट्स से कि आपकी खुद की एप्लीकेशन है उसको एक्सेस कर सकता है तो यह पब्लिक लिए अभी अवेलेबल है अभी देखो एक चीज है कि आप सर्विस को ना अपडेट भी कर
8:46:078 hours, 46 minutes, 7 secondsसकते हो लेट्स से कि अभी हम लोग ने सर्विस बनाई थी एंड उसमें मैंने दो इंस्टेंसस दे दिए थे अभी मान लो कि मुझे उसको चेंज करना
8:46:158 hours, 46 minutes, 15 secondsहै तो मैं क्या कर सकता हूं लेट्स सी यहां पे देखो अपडेट सर्विस का जो ऑप्शन है उसपे आ जाओ ओके एंड यहां पे आपको सेम चीज
8:46:228 hours, 46 minutes, 22 secondsदिखेगी एंड अभी क्या करो ये जो डिजायर्ड टास्क है मैं लेट्स से कि मेरा मूड बदल गया अब मुझे सिर्फ एक ही चाहिए ओके सो चलो
8:46:308 hours, 46 minutes, 30 secondsमैं वन कर देता हूं एंड अपडेट कर देता हूं मैं सिंपली ये जो माय क्लस्टर है ना इस पे चले जाता हूं एंड ये सर्विसेस है एंड टास्क पे जाता हूं सो आप देखोगे यहां पे
8:46:388 hours, 46 minutes, 38 secondsमैंने रिफ्रेश किया सो दो इंस्टेंस से मैं एक इंस्टेंस पे आ गया ठीक है तो आप ऐसे ऑन
8:46:458 hours, 46 minutes, 45 secondsडिमांड अपनी जो सर्विसेस है उसको अपग्रेड भी कर सकते हो ओके एट द एंड ऑफ दिस वीडियो मैं आपको यह भी दिखा देता हूं कि खर्चा कितना हुआ है अभी तक जितना भी किया है ठीक
8:46:548 hours, 46 minutes, 54 secondsहै सो यू जितनी भी अभी टेस्टिंग हुई है उसका आप खर्चा देख सकते हो 144 ठीक है तो वैसे काफी मिनिमम है एंड वन
8:47:038 hours, 47 minutes, 3 secondsमोर थिंग कि मैंने जो अभी एक एग्जांपल दिखाया उसके पहले मैंने थोड़ी बहुत टेस्टिंग और थोड़ा सा ठीक है खुद भी यूज
8:47:098 hours, 47 minutes, 9 secondsकिया है तो उसका मिला के है एंड एक बार हमारी जब टेस्टिंग हो जाती है तो हम जितने भी सारे इंस्टेंसस है उसको रिमूव कर सकते हैं सो दैट हमारा एडिशनल चार्जेस ना हो और
8:47:188 hours, 47 minutes, 18 secondsये चीज जरूर करना ऐसा ना हो कि आपने बना दिया उसको भूल गए छोड़ दिया ठीक है तो ये चीज मत करना सो सबसे पहले पहले क्या करना
8:47:258 hours, 47 minutes, 25 secondsहै क्लस्टर माय क्लस्टर प जाते हैं ये जो सर्विस है ठीक है पहले तो इसको क्लिक कर लो सर्विस एंड यहां पर डिलीट सर्विस ठीक है फोस डिलीट करते हैं बिकॉज उसके अंदर
8:47:338 hours, 47 minutes, 33 secondsहमारा टास्क चल रहा है सो हम सिंपली यहां पर डिलीट करते हैं सो जो हमारा टास्क है वो स्टॉप हो जाएगा और सर्विस यहां देखो
8:47:408 hours, 47 minutes, 40 secondsदेयर नो सर्विस टास्क डेफिनेशन में चलते हैं टास्क पर क्लिक कर लो इसको क्लिक करो
8:47:478 hours, 47 minutes, 47 secondsऔर एक्शन में डी रजिस्टर कर दो ओके सो यहां पर देखोगे टास्क भी हट गए ठीक है सिंपल आप यहां पे आगे रिफ्रेश करोगे कोई
8:47:548 hours, 47 minutes, 54 secondsटास्क नहीं है अब सिंपल लास्ट में बचा हमारा क्लस्टर एंड क्लस्टर पे आप क्लिक करो एंड यहां पे डिलीट क्लस्टर कर दो एंड
8:48:018 hours, 48 minutes, 1 secondयहां पे आपको एक बार नाम लिखना पड़ता है डिलीट माय क्लस्टर एंड डिलीट ओके सो यहां पे हमारा क्लस्टर भी डिलीट हो चुका है
8:48:108 hours, 48 minutes, 10 secondsक्लोज करता हूं एंड यहां पे आप सिंपली रिफ्रेश करो क्लस्टर पे जाते हैं तो कोई भी क्लस्टर नहीं है सो इस तरह से जब आपकी टेस्टिंग हो जाती है तो ये सारे ही
8:48:188 hours, 48 minutes, 18 secondsइंस्टेंसस आप रिमूव कर देना ऑलराइट सो वीडियो यहीं तक ही है आई होप आपको वीडियो पसंद आई होगी इन दैट केस हिट दैट लाइक बटन एंड चैनल पहली बार आ हो तो सब्सक्राइब
8:48:258 hours, 48 minutes, 25 secondsजरूर कर देना थैंक्स फॉर वाचिंग चलिए इस सेक्शन में बात करते हैं amazonaws.com
Chapter 26: AWS EKS (Elastic Kubernetes Servie)
8:48:548 hours, 48 minutes, 54 secondsसिंगल कमांड को यूज करके पूरा कुटी सेटअप कर सकते हो रन कर सकते हो इट इज वेरी इजी टू यूज एंड फास्ट भी है काफी सारी चीजें
8:49:038 hours, 49 minutes, 3 secondsइसमें प्रोवाइड होती है लेकिन अगर आप बिगनर हो सो कुब भी थोड़ा सा हम समझ लेते हैं कुब क्या होता है कि लेट्स से अगर आप
8:49:118 hours, 49 minutes, 11 secondsडिसाइड करते हो अपनी एप्लीकेशन को कंटेनराइज्ड फॉर्म में डिप्लॉयड इज एन ओपन सोर्स प्लेटफॉर्म है
8:49:208 hours, 49 minutes, 20 secondsजो आपके ऑटोमेट कर देता है डिप्लॉयड स्केल एंड मैनेजमेंट ऑफ यर कंटेनराइज्ड एप्स
8:49:278 hours, 49 minutes, 27 secondsजैसे कि लेट्स से डॉकर कंटेनर है आपने एक ऐप बना दिया उसको इमेज उसकी इमेज बना दी डॉकर कंटेनर रेडी है उसके बाद उसको लाइक
8:49:368 hours, 49 minutes, 36 secondsडिप्लॉयड करना है काफी सारे चैलेंज आते हैं ठीक है अब जैसे कि मान लो स्केलिंग करना हो लोड बैलेंसिंग करना हो मैनेजमेंट करना हो बैकअप करना हो सो मेनी थिंग्स
8:49:448 hours, 49 minutes, 44 secondsराइट क्योंकि आपकी भाई अगर प्रोडक्शन की बात कर रहे हैं तो यह काफी सारे फैक्टर है जो कि कोटी आपके इजी कर देता
8:49:538 hours, 49 minutes, 53 secondsहै अब तो सिंपल सा स्ट्रक्चर अग समझे कुब निटस के बारे में जब भी हम एक क्लस्टर बनाते हैं कुब निटस के तो हमको दो वहां पर
8:50:018 hours, 50 minutes, 1 secondइंपॉर्टेंट कंपोनेंट्स मिलते हैं जो कि है अ मास्टर जिसको हम कंट्रोल प्लेन भी बोलते हैं सेकंड चीज है अपना वर्कर नोट्स ओके
8:50:108 hours, 50 minutes, 10 secondsइसको एक डायग्राम से समझते हैं जब भी हम एक कोमेनिटी को सेट अप करते हैं हमें एक पूरा इस तरह से एक पूरा क्लस्टर मिलता है
8:50:188 hours, 50 minutes, 18 secondsठीक है इसके अंदर एक मास्टर नोट होता है बाकी वर्कर नोट्स होते हैं सो आपका एप्लीकेशन जो जो है वो वर्क नोड में रन
8:50:268 hours, 50 minutes, 26 secondsहोता है लेकिन उसमें भी काफी सारी चीजें मैनेज करनी पड़ती हैं जैसे कि लेट्स से यहां पे जब हम नोड बोल रहे हैं तो आप एक
8:50:338 hours, 50 minutes, 33 secondsसर्वर ठीक है अज्यू कर सकते हो सो यहां पे देखोगे कि दो से तीन सर्वर करने हो अगर उसको स्केल करना हो स्केल अप करना है
8:50:418 hours, 50 minutes, 41 secondsस्केल डाउन करना है ओके हाई अवेलेबिलिटी बैकअप कि मान लो अगर एक इंस्टेंस बंद हो गया तो दूसरा इंस्टेंस शुड बी यू नो कमिंग
8:50:488 hours, 50 minutes, 48 secondsअप सो ये सारी चीजें कौन मैनेज करता है ये मास्टर ठीक है तो इसके हाथ में सारा कंट्रोल होता है इसलिए इसको कंट्रोल प्लेन
8:50:558 hours, 50 minutes, 55 secondsभी बोलते हैं ठीक है तो आप थोड़ा सा स्ट्रक्चर समझ पाओगे और यही चीज में अ ईएस जो सर्वेस है ईडब्ल्यूएस की वो हमारी मदद
8:51:048 hours, 51 minutes, 4 secondsकरता है ऑल दो मैं आपको बता दूं ईएस जो है वो ये जो आपका जो मास्टर जो नोड है या
8:51:128 hours, 51 minutes, 12 secondsतो कंट्रोल प्लेन उसको मैनेज करता है ओके एंड आल्सो इसमें एक और फायदा है जैसे कि जो मास्टर है आप देखोगे कि ये सबसे
8:51:208 hours, 51 minutes, 20 secondsइंपॉर्टेंट मतलब आप समझो पूरा क्लस्टर का ब्रेन जो है वो मास्टर नोड है राइट अगर वही मान लो डेड हो गया क्रैश हो गया तो
8:51:288 hours, 51 minutes, 28 secondsआपका तो पूरा क्लस्टर ही गया लेकिन अभी एडब्ल्यू में क्या होता है हमारे पास
8:51:358 hours, 51 minutes, 35 secondsरीजंस होते हैं रीजंस के बाद हमारे पास एजीस होते हैं अवेलेबिलिटी जनस होते हैं मल्टीपल सो जब आप यहां पे मास्टर नोड
8:51:438 hours, 51 minutes, 43 secondsबनाते हो ईएस की मदद से तो जो मास्टर नोड भी है व अलग-अलग अवेलेबिलिटी जनस में रेप्स होते हैं सो दैट आपका जो सिंगल
8:51:528 hours, 51 minutes, 52 secondsपॉइंट ऑफ फेलियर का जो इश्यूज है वो यहां पे रिमूव हो जाता है तो यह कुछ बेनिफिट्स हैं जो हमको एडब्ल्यू में या तो ईएस में
8:52:008 hours, 52 minutesमिल जाते हैं ओके सो अगर आप देखोगे एडब्ल्यू का वीपीसी जो उनका खुद का डिफॉल्ट रहता है उसके अंदर आपका ईएस
8:52:088 hours, 52 minutes, 8 secondsकंट्रोल प्लेन जो कि मास्टर नोड है वह बन जाता है सो दैट एडब्ल्यू उसको मैनेज करता है नाउ लेकिन आपकी एप्लीकेशन कहां चलेगी
8:52:178 hours, 52 minutes, 17 secondsवोह आपको पता है जो कि वर्कर नोट्स वर्कर नोट्स के लिए यहां पे दो चीजें यूज़ होती हैं एडब्ल्यू में एक तो होता है टू
8:52:248 hours, 52 minutes, 24 secondsइंस्टेंस जोकि हमें पता है कैसे वर्क करता है ठीक है इसमें रिसोर्स आप होते हैं एमआई होती हैं एंड स्टोरेज होता है या तो ईजी
8:52:328 hours, 52 minutes, 32 secondsटू इंस्टेंस होगा या तो फार गट जो सर्वरलेस अप्रोच है दोनों ही अप्रोच से हम इसको एक्सेस कर सकते हैं फार गट में सिंपल
8:52:418 hours, 52 minutes, 41 secondsहै आपको पता है कि आपको यह कोई भी हेडेक नहीं होता है ईजी टू इंस्टेंस मैनेज करो उसको कॉन्फिडो यह सारा हेडेक खत्म दूसरी
8:52:498 hours, 52 minutes, 49 secondsचीज सर्वरलेस में जितनी जरूरत है उतना ही रिसोर्स यूज होता है सो दैट वहां पे कॉस्ट भी कम हो जाता है सो दो अप्रोच है दोनों
8:52:578 hours, 52 minutes, 57 secondsही अप्रोच हम लोग यूज कर सकते हैं ओके यह एक और एक डायग्राम है जो आपको स्लाइड्स में मिल जाएगा सो दैट इसमें आपको थोड़ा सा
8:53:068 hours, 53 minutes, 6 secondsऔर ज्यादा अंडरस्टैंडिंग होगी कि कोमेनिटी एडमिन है वो सबसे पहले सिंपल क्या है एक क्लस्टर बनाएगा ठीक है कैनिटीज का फिर आप
8:53:148 hours, 53 minutes, 14 secondsदेख रहे हो यहां पे पब्लिक सबनेट होता है प्राइवेट सबनेट होता
8:53:268 hours, 53 minutes, 26 secondsहै पब्लिक सबनेट अगर आप देखोगे अगर आपकी एप्लीकेशन को इंटरनेट से एक्सेस करना है तो पब्लिक एक्सेस इज विल बी यूजफुल यूज्ड
8:53:338 hours, 53 minutes, 33 secondsएंड बाकी जो आपके वर्क नोट्स हैं वो प्राइवेट में ही रन होंगे ओके इसमें नेट यूज होता है नेट आईपीस यूज होती हैं एंड
8:53:428 hours, 53 minutes, 42 secondsइंटरनेट गेटवे यूज होते हैं ठीक है मतलब काफी सारे मतलब बिहाइंड द सीन ये इस तरह से वर्क करता है प्रैक्टिकल करने से पहले
8:53:508 hours, 53 minutes, 50 secondsजो सबसे इंपॉर्टेंट फैक्टर है जो मैं आपको नोट या तो डिस्क्लेमर दे देना चाहता हूं कि ये अ मतलब पेड सर्विस है ओके अब पेड
8:53:588 hours, 53 minutes, 58 secondsसर्विस है तो प्राइसिंग की हम लोग यहां पे डायरेक्टली बात कर लेते हैं यहां पे इएस का इनका ऑफिशियल डॉक्यूमेंटेशन है प्राइसिंग हम लोग देख लेते हैं गेट
8:54:068 hours, 54 minutes, 6 secondsस्टार्टेड फॉर फ्री लिखा तो है लद अगर आप देखोगे एडब्ल्यू एस प्राइसिंग कैलकुलेटर पे जा सकते हो कुने वर्जन सपोर्ट
8:54:158 hours, 54 minutes, 15 seconds0 पर आर के हिसाब से यहां पर प्राइसिंग लगती है दैट इज द स्टैंडर्ड ओके ऑटो मोड
8:54:238 hours, 54 minutes, 23 secondsमें कितना क्या लग है अगर आप नीचे देखोगे सो ऑटो मोड ठीक है तो एक अलग मोड है जो ऑटोमेटिक सारी चीजें कॉन्फिन कर देता है
8:54:318 hours, 54 minutes, 31 secondsलेकिन मैंने जैसे कि बताया कि भाई वर्क नट क्या होता है सिंपल एक ईसी टू इंस्टेंस ही होता है ठीक है अब जैसे कि मैं सिंपल बताऊं अ ये जो तीन सर्वर लेट से देख रहे
8:54:418 hours, 54 minutes, 41 secondsहो तो यहां समझ लो तीन ट इंस्टेंस बना देगा और तीनों में आपके कंटेनर बेस्ड एप्लीकेशन को रन करेगा अब तीन ट बन रहा है
8:54:498 hours, 54 minutes, 49 secondsअब e2 में भी देखो अलग-अलग टाइप के इंस्टेंस हो सकते हैं इंस्टेंस टाइप अगर आप देखोगे x लार्ज ओके फ्स लार्ज लार्ज मीडियम एंड ऑल उसके
8:54:588 hours, 54 minutes, 58 secondsबेसिस पर यह प्राइसिंग यहां पर चेंज होती है इट्स मेक्स सेंस है ना कि जितना पावरफुल आपका रिसोर्स होगा उतना प्राइस उस
8:55:078 hours, 55 minutes, 7 secondsहिसाब से लगेगा इट्स वेरी स्ट्रेट फॉरवर्ड ओके एंड ऑफकोर्स वो चीज आप कंट्रोल कर सकते हो कि आपको कैसा मतलब कितना क्या
8:55:168 hours, 55 minutes, 16 secondsचाहिए एंड ऑल जैसे कि अगर आप देखोगे प्राइसिंग एग्जांपल यहां पे ईएस ऑटो मोड पे अगर आप जाते हो सो य पे क्या डिफरेंसेस
8:55:248 hours, 55 minutes, 24 secondsएंड क्या थोड़ा सा आप देख सकते हो एंड एक सबसे बड़ा डिफरेंस कि ऑटो मोड वर्सेस इजी टू इंस्टेंस जो कि आपका सेल्फ मैनेज्ड है
8:55:338 hours, 55 minutes, 33 secondsआप खुद कंट्रोल करना चाहते हो उस चीजों पे तो वो भी आप कर सकते हो क्योंकि अगर आप चाहते हो कि भाई आपके एप्लीकेशन बहुत
8:55:398 hours, 55 minutes, 39 secondsहाइपर सेंसिटिव है बिजनेस ओरिएंटेड है कि मतलब वहां पे लॉस नहीं हो सकता डाउन टाइम
8:55:468 hours, 55 minutes, 46 secondsनहीं हो सकता ठीक है लेटेंसी नहीं हो सकती सो वहां पे आपको ऑफकोर्स ज्यादा अच्छे तगड़े रिसोर्सेस चाहिए होंगे तो उसमें आप ज्यादा अच्छे सोर्स करोगे और यहां पे साइड
8:55:558 hours, 55 minutes, 55 secondsबाय साइड नॉर्मल आप देख सकते हो कि अ डिफरेंस किसमें क्या होता है ओके एंड
8:56:038 hours, 56 minutes, 3 secondsइसमें एक कैलकुलेटर भी होता है जैसे कि अ जैसे कि ये है ठीक है
8:56:118 hours, 56 minutes, 11 secondsamazon2 यूए जो कि एप्रोक्सीमेटली आपका मंथली एक्सपेंस होगा ओके सो ये तो रही प्राइसिंग की बात एंड चलिए अब आगे बढ़ते
8:56:198 hours, 56 minutes, 19 secondsहैं और प्रैक्टिकल करके देखते हैं चलिए अब प्रैक्टिकल करते हैं एंड इएस क्लस्टर बनाते हैं और इससे आगे बढ़ने से पहले आपके
8:56:278 hours, 56 minutes, 27 secondsपास यह तीन चीजें सेटअप होनी चाहिए ठीक है पहली चीज तो य एडब्ल्यू सीआई ओके क्योंकि हम लोग कमांड लाइन से ये क्रिएट करने वाले
8:56:358 hours, 56 minutes, 35 secondsहैं एंड ऑफकोर्स कॉन्फिन क्रेडेंशियल कि आपको यूजर क्रेडेंशियल बनाने मैं अभी आपको
8:56:418 hours, 56 minutes, 41 secondsबता दूंगा इसके बाद ई ईएस सीटीएल ये और क्यूब सीटीएल ठीक है दो चीजें आपके पास
8:56:488 hours, 56 minutes, 48 secondsइंस्टॉल होनी चाहिए चलिए सबसे पहले शुरुआत करते हैं एडब्ल्यू ए सल आई के साथ ठीक है तो सीआ आप मतलब ये स्लाइड में आपको
8:56:578 hours, 56 minutes, 57 secondsइंस्ट्रक्शन मिल जाएंगे यह एक लिंक दी हुई है जहां से आप सीआई इंस्टॉल कर सकते हो विज हो या तो मैक हो ओके एंड एमएसआई
8:57:058 hours, 57 minutes, 5 secondsपीकेजी सिंपल इंस्टॉल करना है इसके बाद जो आपका सेकंड स्टेप होगा वो है क्रेडेंशियल सेटअप कि आप अपने मतलब जो भी सिस्टम है
8:57:158 hours, 57 minutes, 15 secondsउसके कमांड लाइन से टर्मिनल से अपने एडब्ल्यू के रिसोर्सेस को एक्सेस कर सको ऑल दो इसपे सेपरेट वीडियो है बट मैं जल्दी
8:57:228 hours, 57 minutes, 22 secondsसे यहां पर कवर कर देता हूं ठीक है यह मेरा टर्मिनल है लद मैं वीएस कोड यूज़ कर रहा हूं एंड मैं डायरेक्टली चलता हूं अपने
8:57:298 hours, 57 minutes, 29 secondsएडब्ल्यू के डैशबोर्ड पे एंड सबसे पहले क्या करना है आईएएम पे जाना है ओके आइडेंटिटी एंड एक्सेस मैनेजमेंट एंड यहां
8:57:378 hours, 57 minutes, 37 secondsपे यूजर्स पे चले जाना है ठीक है यूजर्स पे ऑल दो ऑलरेडी मेरा यूजर बना हुआ है ठीक है अगर नहीं बना है तो आप क्रिएट एक यूज़र
8:57:448 hours, 57 minutes, 44 secondsकर लेना जैसे कि एक यूजर बना हुआ है ए ठीक है एंड लद क्योंकि देखो हम लोग यहां पे काफी सारी चीजें कर रहे हैं तो मैंने क्या
8:57:518 hours, 57 minutes, 51 secondsकिया यहां पे एडमिन एक्सेस उसको देके रखा ठीक है इसके पास एडमिन एक्सेस है एंड अगर नहीं है आपके केस में अगर न्यू यूजर है तो
8:58:008 hours, 58 minutesठीक है तो ऐड परमिशन यहां से कर लेना और एडमिन एडमिनिस्ट्रेटर एक्सेस आप दे देना इसके बाद सिंपली आपको जाना है सिक्योरिटी
8:58:078 hours, 58 minutes, 7 secondsक्रेडेंशियल एंड इसमें एक्सेस कीज ठीक है तो क्रिएट एक्सेस की कमांड लाइन इंटरफेस से हम लोग को यूज करना है तो सेलेक्ट एंड
8:58:158 hours, 58 minutes, 15 secondsआई अंडरस्टैंड एंड नेक्स्ट इसके बाद एक सिंपल सी एक आप इसको नाम दे दो जैसे कि मैं दे देता हूं ईएस सीटीएल के लिए मैं
8:58:248 hours, 58 minutes, 24 secondsबना रहा हूं क्रिएट एक्सेस एंड इसके बाद आपको यहां पे एक एक्सेस की और सीक्रेट एक्सेस की मिल जाएगी ओके नाउ आई एम अजूम
8:58:338 hours, 58 minutes, 33 secondsकि आपके पास जो एडब्ल्यू ए सीएल आई है वो इंस्टॉल हो चुका है ओके तो इसके बाद आपको सिंपल करना है एड कॉन्फिन ओके सो जैसे ही
8:58:438 hours, 58 minutes, 43 secondsआप ये कमांड यूज करते हो सो आप देखोगे एक एक्सेस की आपको यहां पे मांग रहा है ठीक है ऑलरेडी मेरे पास कॉन्फ़िगर था इसलिए दिखा रहा है ऑल दो मैं इसमें नई वाली
8:58:518 hours, 58 minutes, 51 secondsएक्सेस कर देता हूं देखो एक्सेस की है इसको यहां पर कॉपी किया एंड सिंपली यहां पे पेस्ट किया एंड इसके बाद सीक्रेट एक्सेस की तो ये जो सीक्रेट एक्सेस की है
8:59:008 hours, 59 minutesइसको मैंने कॉपी किया एंड सिंपल यहां पे पेस्ट कर दिया ओके डिफॉल्ट रीजन क्या आपको सेट करना है क्योंकि अगर आप रीजन प्रोवाइड
8:59:078 hours, 59 minutes, 7 secondsनहीं करोगे तो डिफॉल्ट ले लेगा ऑल दो ईयू ठीक है मैं नॉर्थ वन जो कि मेरा स्टॉक होम का है ठीक है जैसे कि आप यहां से ग्लोबल
8:59:158 hours, 59 minutes, 15 secondsमतलब यहां से चेक कर सकते हो रीजन ये देखो ये सारे हैं ठीक है अभी स्टॉक होम है तो य नॉर्थ वन जो भी आपका है जैसे मान लो अगर मुंबई है या तो कोई भी है तो आप वो वाला
8:59:238 hours, 59 minutes, 23 secondsदे सकते हो ठीक है तो यह यहां पर आपने तीन चीज दे दी इसको मैं कर देता हूं डिफॉल्ट
8:59:298 hours, 59 minutes, 29 secondsआउटपुट फॉर्मेट ठीक है इसको ब्लैंक दैट्ची
8:59:548 hours, 59 minutes, 54 secondsऔर आपका यूजर का नाम भी दिख जाएगा दैट मींस आपका जो यूजर का मतलब जो एडब्ल्यू का सेटअप हो चुका है कंप्लीट और यहां पर हमारा काम हो गया तो मैं डन कर देता हूं
9:00:029 hours, 2 secondsऑल दो अगर आपको ये बार-बार भी आगे भी यूज़ करना है तो डाउनलोड ड सीएबी फाइल कर लेना लद मैं टेस्टिंग परपस है तो इसलिए
9:00:109 hours, 10 secondsकंटिन्यू कर रहा हूं दैट्ची है जी हमारा एडब्ल्यू सेटअप हो गया ओके और यहां से हम लोग ने चेक भी कर लिया
9:00:189 hours, 18 secondsअब आ जाते हैं नेक्स्ट स्टेप पे जो कि है ई एसीटीएल ओके तो यह हमको इंस्टॉल करना है
9:00:259 hours, 25 secondsइसका सिंपल परपस यह होता है कि हम सील को यूज करके अ एए पे एक अपना क्यूब निटीज
9:00:349 hours, 34 secondsबेस्ड क्लस्टर बना पाए एक इसका सिंपल सा यह यूज केस है एंड सबसे इजस्ट वे जो मुझे लगता है जो मैं रिकमेंड भी करता हूं वो है
9:00:429 hours, 42 secondsअगर आप
9:00:549 hours, 54 secondsवेरी सिंपल और मैक वाले में b.s. s को सेटअप कर लो एंड इसके बाद ब्रू
9:01:049 hours, 1 minute, 4 secondsटैप मतलब यहां पे आपको कमांड्स दिख जाएंगी ू इंस्टॉल वे वर्क टैप ई ए सीटीएल ओके सो
9:01:119 hours, 1 minute, 11 secondsइसके बाद आई एम अजूम कि आपके पास ई एस सीटीएल इंस्टॉल हो चुका है ठीक है अब यहां पे चकलाते एंड ब डटस एच क्यों बोला है
9:01:209 hours, 1 minute, 20 secondsमैंने क्योंकि देखो टीए यहां पे इंस्टॉल हो गया आई एम अजूम मिंग ठीक है इसको फॉलो कर लेना बहुत इजी है इसके बाद अब मान लो
9:01:299 hours, 1 minute, 29 secondsइसके बाद अपन को क्यूब सीटीएल भी इंस्टॉल करना पड़ेगा अब अगर आपके पास दोनों चीजें हैं जैसे पे मैक पे बू और विज पे चौगल आते
9:01:389 hours, 1 minute, 38 secondsकोई भी क्यूब सीटीएल इंस्टॉल करना है यहां पे देखो डॉक्यूमेंटेशन की लिंक मैंने दे दी है और इसके बाद मैक में ब्रू इंस्टॉल
9:01:449 hours, 1 minute, 44 secondsक्यूब सीटीएल विंडोज में चको इंस्टॉल कनेट सीआई वेरी सिंपल ठीक है सो यहां पे इस स्टेज पे आई एम
9:01:529 hours, 1 minute, 52 secondsएज्यूमंगा चीज हो गई है रेडी ओके नाउ अब इसके बाद हमारा सबसे पहले जो स्टेज या तो
9:02:019 hours, 2 minutes, 1 secondस्टेप होगा वोह है क्रिएटिंग ईएस क्लस्टर ठीक है जी उसके लिए बहुत ही सिंपल सी
9:02:069 hours, 2 minutes, 6 secondsकमांड है ईएस सीटीएल लद अब यहां पे ई एस सीटी एल ठीक है इस तरह से मैं मैं अगर
9:02:159 hours, 2 minutes, 15 secondsसिंपल इतना कर दूं तो आप देखोगे सारी स्टेप्स यहां पे आ जाएंगी लद मतलब दिस इज आल्सो अ वे टू चेक कि इट इज इंस्टॉल्ड र
9:02:229 hours, 2 minutes, 22 secondsनॉट एंड इस इन द सेम वे आप क्यूब सीटीएल भी देखो क्यूब सीटीएल मैंने किया तो आप देखोगे इस टाइप से यहां पर मुझे यहां पे
9:02:309 hours, 2 minutes, 30 secondsऑप्शंस दिखाई दे रहा है दैट मींस कि ये क्यूब सीटीएल वगैरह इंस्टॉल्ड है ओके चलिए इसके बाद डायरेक्टली मैं चलता हूं डैशबोर्ड पे एंड आपको कुछ दिखाना चाहता
9:02:399 hours, 2 minutes, 39 secondsहूं यहां पे इलास्टिक कुटी सर्विस यहां पे आप सिंपल ईएस भी आप सर्च कर सकते हो इलास्टिक कुब एटी सर्विस मैं इस पर
9:02:489 hours, 2 minutes, 48 secondsक्लिक करता हूं एंड आपको बस इतना दिखाना है कि यहां पे अभी जीरो क्लस्टर है ओके नाउ अभी हम लोग क्या है एक क्लस्टर
9:02:569 hours, 2 minutes, 56 secondsबनाएंगे कोबस का सिंपल सी एक कमांड आपको यूज करनी है ई सीटीएल क्रिएट क्लस्टर चलो अपन यूज करते
9:03:059 hours, 3 minutes, 5 secondsहैं ठीक है क्रिएट क्लस्टर हाइन हाइफ नेम इक्वल टू नेम आप अपने हिसाब से दे सकते हो
9:03:149 hours, 3 minutes, 14 secondsअब जैसे कि मान लो मैं दे देता हूं माय क्लस्टर दे देते हैं लेट्स से एंड इसके बाद एक यहां पर हम लोग फ्लैग यूज
9:03:219 hours, 3 minutes, 21 secondsकरेंगे जो कि है इनेबल ऑटो मोड एंड इसका क्या मतलब है मैं आपको बताऊंगा अभी ऑटो
9:03:299 hours, 3 minutes, 29 secondsमोड देखो इनेबल ऑटो मोड में क्या होता है कि यह ऑटोमेटिक सारी चीजें कॉन्फिन आपको मैनेज करने की जरूरत नहीं है
9:03:389 hours, 3 minutes, 38 secondsमतलब दिस इज द इजस्ट वे मतलब वो अपनी तरफ से कस्टमाइज करके आपके लिए एक क्लस्टर बना के दे देता है इंक्लूडिंग द वर्कर नोट्स
9:03:459 hours, 3 minutes, 45 secondsवर्कर नोट्स भी बना के देता है ओके सो मैं जैसे ही एंटर करता हूं सो यहां पे कुछ टाइम लगता है ओके एंड मतलब कुछ मतलब 45
9:03:549 hours, 3 minutes, 54 secondsमिनट्स तक भी लग सकते हैं सो जब यहां पे क्लस्टर बन रहा है सो आप काफी सारी इसके रिलेटेड इंफॉर्मेशन यहां पे देख सकते हो जैसे कि देखो ईए सीटीएल वर्जन एनीवे इट्स
9:04:039 hours, 4 minutes, 3 secondsफाइन किस रीजन में काम हो रहा है ओके इसके बाद यू विल सी कि अवेलेबिलिटी जनस कौन-कौन से हैं एजज एंड यहां पे सबनेट आप देख सकते
9:04:129 hours, 4 minutes, 12 secondsहो यहां पे पब्लिक सबनेट एंड प्राइवेट सबनेट दोनों ही बना के दे रहा है क्योंकि ऑफकोर्स हमारी एप्लीकेशन को बाहर से
9:04:209 hours, 4 minutes, 20 secondsएक्सेस करने की जरूरत पड़ सकती है तो पब्लिक सबनेट की भी यहां पे व्यवस्था है कोबन 80 वर्जन कौन सा यूज कर रहा है यू कैन आल्सो सी दैट वह भी आप कंट्रोल कर
9:04:299 hours, 4 minutes, 29 secondsसकते हो अगर आपको कोबन 80 का कोई स्पेसिफिक वर्जन यूज करना है देन आल्सो यू कैन डू इट ओके क्रिएटिंग ईएस क्लस्टर अगर
9:04:389 hours, 4 minutes, 38 secondsआप देखोगे माय क्लस्टर नाम है और यहां पर किस रीजन में बन रहा है आप देख सकते हो ऑफकोर्स यह बिहाइंड द सीन देखो यह ऑटोमेशन
9:04:459 hours, 4 minutes, 45 secondsजैसा है तो य क्लाउड फॉर्मेशन का ही यूज कर रहा है ओके एंड इसमें काफी सारी मतलब अ
9:04:539 hours, 4 minutes, 53 secondsतो देखो बिल्डिंग डिप्लॉयड यह सारी चीजें चल रही हैं एक बार जब ये मतलब यह काम हो जाएगा ना तो यहां पे आपको क्लस्टर दिखाई
9:05:009 hours, 5 minutesभी देगा सी आई थिंक अभी अभी से ही हम देख सकते हैं कि यहां पे जो ईएस है इसमें एक क्लस्टर जो हमारा माय क्लस्टर है वो बन
9:05:089 hours, 5 minutes, 8 secondsचुका है बट क्रिएट यहां पे स्टेटस क्या है क्रिएटिंग मतलब अभी बना नहीं है ओके सो कुछ देर के बाद आपको कुछ इस टाइप का आएगा
9:05:159 hours, 5 minutes, 15 secondsकि आपका जो क्लस्टर है वो इस रीजन में रेडी है ओके सो यहां पे अपना क्लस्टर बनके रेडी हो चुका है मैं इसको क्लियर क्लियर
9:05:239 hours, 5 minutes, 23 secondsकरता हूं अब देखो एक बार जब आपका क्लस्टर बन गया है तो लेट्स से कि आपका जो कनेट का क्लस्टर है इट इज अप एंड रनिंग नाउ इसके
9:05:329 hours, 5 minutes, 32 secondsबाद जो भी आपकी क्यूब सीटीएल बेस्ड कमांड्स है आप काफी आसानी से यूज कर सकते हो जैसे कि क्यूब सीटीएल ओके
9:05:399 hours, 5 minutes, 39 secondsअ क्लस्टर इंफो कि आपको क्लस्टर के बारे में ठीक है इंफॉर्मेशन चाहिए सो यू कैन सी
9:05:469 hours, 5 minutes, 46 secondsदिस कि देखो कुबे कंट्रोल प्लेन इज रनिंग एट क्योंकि कंट्रोल प्लेन मतलब मास्टर नोड
9:05:539 hours, 5 minutes, 53 secondsजो था हमारा वो यहां पे रनिंग है और आप देख सकते हो . amazonaws.com ऑलराइट सो इस स्टेज तक हमारा जो कुटज
9:06:019 hours, 6 minutes, 1 secondक्लस्टर है वो अप एंड रनिंग है इन द क्लाउड एंड आप उसको मतलब अपने लोकल सिस्टम से उसको सीआई को यूज करके आप इसको एक्सेस
9:06:109 hours, 6 minutes, 10 secondsभी कर पा रहे हो लेकिन अ इसके बाद काफी जो जर्नी है काफी इजी है क्यूब सीटीएल को यूज़ करके आप सिंपल अपने एप्लीकेशन के डिप्लॉयड कर सकते हो वेरी इजली ऑल दो एक
9:06:199 hours, 6 minutes, 19 secondsसिंपल एप्लीकेशन को हम लोग रन करके देखते हैं तो थोड़ा सा आपको और आईडिया आएगा अ इनके पास जो इनकी ऑफिशियल डॉक्यूमेंटेशन
9:06:269 hours, 6 minutes, 26 secondsहै क्विक स्टार्ट गाइड को मैं रेफरेंस एज अ यूज़ कर रहा हूं एंड यहां पे इंस्ट्रक्शन दिए हैं दैट इज वेरी इजी ठीक है सो हम लोग नीचे जाते हैं इन दिस
9:06:349 hours, 6 minutes, 34 secondsट्यूटोरियल क्या-क्या होने वाला है क्या-क्या चीजें होंगी एंड स्टार्टिंग में आपको किन-किन चीजों की जरूरत है एडब्ल्यू एसीएल आई ओके क्रेडेंशियल कॉन्फ़िगर करना
9:06:429 hours, 6 minutes, 42 secondsहै वो हम लोग ने कर चुके हैं ईएस सीटीएल एंड क्यूब सीटीएल ओके वेरी सिंपल यह सारी चीजें हमारी हो गई कॉन्फ़िगर द क्लस्टर
9:06:509 hours, 6 minutes, 50 secondsअगर आप देखोगे सबसे पहले स्टेप क्या है क्रिएट क्लस्टर जो हमारा ऑलरेडी हो चुका है लेकिन इसमें इन लोगों ने सिंपल एक चीज किया है ये एक फाइल को यूज किया है लेकिन
9:06:599 hours, 6 minutes, 59 secondsहम लोग ने कमांड से डायरेक्टली क्लस्टर बनाया था इसके बाद सिंपल ए लोग क्या कर रहे हैं इंग्रेस क्लास बना रहे हैं अब
9:07:069 hours, 7 minutes, 6 secondsइसका क्या यूज है सो आप यहां पे पढ़ भी सकते हो कि जो ऑटो मोड है जो हम लोग ने अभी इनेबल किया इंग्रेस क्लास क्या करेगा
9:07:149 hours, 7 minutes, 14 secondsऑटोमेटिक प्रोविजन एंड मैनेज लोड बैलेंसर लोड बैलेंसर को ठीक है प्रोविजन करना बेस्ड ऑन द रिक्वेस्ट और मतलब जो भी आपके
9:07:249 hours, 7 minutes, 24 secondsएप्लीकेशन प लोड आएगा एंड सो ऑन ठीक है सो यह जो फाइल है इसको आप सिंपल कॉपी करो अपने एक लोकल पे बना लेना देखो मैंने
9:07:329 hours, 7 minutes, 32 secondsऑलरेडी एक फाइल बना के रखी है एंड सेम कंटेंट है ठीक है एंड इसके बाद यहां पे आपको सिंपल क्यूब सीटीएल अप्लाई करना है जो हम लोग नॉर्मली क्यूब सीटीएल का जो यूज
9:07:409 hours, 7 minutes, 40 secondsकेस करते हैं क्यूब सीटीएल अप्लाई किया ओके सो यहां पे मेरी फाइल का नाम कुछ अलग
9:07:479 hours, 7 minutes, 47 secondsहै इय जो भी आपका फाइल का नाम है वो देना तो आप देखोगे
9:07:539 hours, 7 minutes, 53 secondsएंड इसके बाद सिंपल है डिप्लॉयड 2048 गेम मतलब जो इनका सैंपल गेम है यहां पे एक यह वाली कमांड है क्यूब सीटीएल यहां पे हम
9:08:019 hours, 8 minutes, 1 secondलोग एक नेम स्पेस क्रिएट कर रहे हैं ओके सो नेम स्पेस क्रिएट हो गया इसके बाद सिंपल आप यहां पे क्यूब सीटीएल अप्लाई
9:08:099 hours, 8 minutes, 9 secondsयहां पे एक यह वाली कमांड यूज करोगे जो कि होगा अप्लाई वाला ओके सिंपल अप्लाई कर दिया एंड देखोगे अप्लाई किया है
9:08:189 hours, 8 minutes, 18 secondsतो आप देखोगे नेम स्पेस डिप्लॉयड सर्विस इंग्रेस रूल ये सारी ही चीज यहां पे प्रोवाइडेड है ओके वेरी इंटरेस्टिंग चीज
9:08:279 hours, 8 minutes, 27 secondsकि अगर आप देखोगे अप्लाई एन एंड यहां पे यह जो आप देख रहे हो ना यह पूरी एक आपको लिंक यहां पे दिख जाएगी मैं इसको कॉपी
9:08:359 hours, 8 minutes, 35 secondsकरता हूं एंड आप इसको ब्राउजर में ओपन कर सकते हो सिंपली मैं यहां पे कॉपी पेस्ट करता हूं सो आप देखोगे इस ये जो फाइल है
9:08:429 hours, 8 minutes, 42 secondsइसका कंटेंट आपको यहां पे दिख जाएगा ठीक है तो इस फाइल में क्या-क्या सारी चीजें हैं सिंपल एक नेम स्पेस बनाया है डिप्लॉयड
9:08:499 hours, 8 minutes, 49 secondsकर रहे हैं इन लोग जिसमें एक कंटेनर यूज किया है कंटेनर आप देख सकते हो पब्लिक ईसीआर ओके और कंटेनर रजिस्ट्री ऑफ
9:08:589 hours, 8 minutes, 58 secondsएडब्ल्यू एस एंड यहां पे जो भी इनका नाम है ठीक है एंड इसके बाद कंटेनर पोर्ट 80
9:09:059 hours, 9 minutes, 5 secondsहै कंटेनर रनिंग है पॉड में इसके बाद उसको एक्सेस करना है तो यहां पे एक सर्विस बनाई
9:09:129 hours, 9 minutes, 12 secondsहै ओके सर्विस में क्या किया 80 पोट मतलब एक रिक्वेस्ट आती है 80 पोर्ट पे एचटीटीपी सो 80 पोर्ट पर डाइवर्ट कर दो वेरी सिंपल
9:09:219 hours, 9 minutes, 21 secondsएंड इसके बाद यहां पे एक इंग्रेस यू रूल यूज किया है जो कि है फॉर द इंटरनेट फेसिंग ओके एंड यहां पे भी हम लोग ने
9:09:309 hours, 9 minutes, 30 secondsसिंपल किया कि एचटीटीपी रिक्वेस्ट है 80 मतलब बाहर इंटरनेट से रिक्वेस्ट आएगी 80 पे सो इसके बाद सर्विस के 80 पोर्ट पे
9:09:379 hours, 9 minutes, 37 secondsजाना फिर सर्विस से आपकी एप्लीकेशन पे जाना अगर कंफ्यूज ंग लग रहा है तो थोड़ा इस डायग्राम से आप समझने की कोशिश करो कि
9:09:449 hours, 9 minutes, 44 secondsयहां पे इंग्रेस एक एडिशनल चीजें हम लोग यूज करिए क्या इंग्रेस क्या करता है एक्सपोज एचटीटीपी एंड एचटीटीपीएस राउट्स
9:09:519 hours, 9 minutes, 51 secondsफ्रॉम आउटसाइड द क्लस्ट जस्ट थिंक अबाउट इट वी आर टॉकिंग अबाउट आउटसाइड द क्लस्टर यहां पर पूरा क्लस्टर
9:09:599 hours, 9 minutes, 59 secondsबना हुआ क्योंकि ऑफकोर्स इंटरनेट जो है वो आउटसाइड द क्लस्टर ही है एंड अगर आप एक आपको ब्राउजर से ऐसे एक्सेस करना है सो
9:10:069 hours, 10 minutes, 6 secondsआपको यह इंग्रेस यूल यूज करना पड़ेगा एंड एक्चुअली इसमें किया हुआ तो मैंने एक ब्रीफ आपको थोड़ा सा ओवरव्यू दे दिया इसके
9:10:149 hours, 10 minutes, 14 secondsबाद आपको सिंपल करना है क्यूब सीटीएल गेट इंग्रेस जो हम लोग ने इंग्रेस बनाया है अभी हम इस पेस्ट करता हूं गेट इंग्रेस हान
9:10:229 hours, 10 minutes, 22 secondsए यह जो नेम स्पेस है वो इसको करते हैं सो अब देखोगे एड्रेस एड्रेस में आपको कुछ इस टाइप से एक लिंक मिलेगी मैं इसको कॉपी
9:10:309 hours, 10 minutes, 30 secondsकरता हूं एंड सिंपली ब्राउजर प जाके इसको पेश करता हूं सो सो हेयर यू गो सो अगर आप देखोगे सो
9:10:379 hours, 10 minutes, 37 secondsहमारी एप्लीकेशन इज आई मीन अप एंड रनिंग और आप देखोगे इस तरह से मैं इसको ठीक है एरो कीज से खेल भी सकता हूं ठीक है ये गेम
9:10:469 hours, 10 minutes, 46 secondsइट इज लिटरली लाइक अ वर्किंग गेम इसी क्विक स्टार्ट गाइड में आपको ये भी दिख जाएगा कि आप इस को फर्द एनहांस कैसे कर
9:10:549 hours, 10 minutes, 54 secondsसकते हो क्योंकि अभी देखोगे हम लोग ने डिप्लॉयड किया चलाया स्पिन किया एंड ऑल एंड इसके बाद अगर पसेस डाटा वगैरह आपको
9:11:029 hours, 11 minutes, 2 secondsयूज करना है सो वो कैसे करोगे वो चीजें भी इसमें दी गई है ठीक है अप्लाई स्टोरेज क्लास जैसे कि इंग्रेस क्लास यूज किया था
9:11:099 hours, 11 minutes, 9 secondsअब स्टोरेज क्लास यूज कर सकते हो ठीक है तो परसिस्टेंट वॉल्यूम आप वहां पे सेटअप कर लेना लद जब हम लोग ने डिप्लॉयड किया तो
9:11:179 hours, 11 minutes, 17 secondsडिप्लॉयड के बाद कुछ चीजें और इसमें होती हैं जो चेंजेज वो मैं आपको दिखा देता हूं कि पहले तो ईएस बना तो अगर आप देखोगे ईएस
9:11:279 hours, 11 minutes, 27 secondsमें हमारा यहां पे क्लस्टर दिख जाता है ठीक है अगर आप देखोगे माय क्लस्टर एक्टिव ठीक है सो यहां प आपको क्लस्टर तो दिख रहा
9:11:349 hours, 11 minutes, 34 secondsहै लेकिन एट द सेम टाइम मैं एडब्ल्यू प जाता हूं एंड ट हम लोग ओपन करते हैं ठीक है टू सो यू विल सी कि यहां प देखो
9:11:429 hours, 11 minutes, 42 secondsइंस्टेंस रनिंग वन है एंड इस पर मैं क्लिक करता हूं सो आपको ये दिखेगा एक इंस्टेंस रनिंग है एंड इसका अ इंस्टेंस टाइप देखोगे
9:11:499 hours, 11 minutes, 49 secondsएड लार्ज है सो आप देख सकते हो कि इसने मतलब यह जो इंस्टेंस है वह इसने ही मतलब जब डिप्लॉयड अपन ने किया तब बना हुआ है सो
9:11:579 hours, 11 minutes, 57 secondsआप देख सकते हो कि अगर अपन लार्ज इंस्टेंस यूज़ करें तो डेफिनेटली बिलिंग इसकी आएगी सो इन चीजों का थोड़ा सा आपको ध्यान रखना है एंड इसके बाद मैं लेफ्ट साइड में जाता
9:12:059 hours, 12 minutes, 5 secondsहूं लोड बैलेंसर सो अगर आप देखोगे k8 गेम 2048 इंग्रेस तो जो अभी हम लोग ने डिप्लॉयड किया इंग्रेस रूल यूज किया उसका
9:12:139 hours, 12 minutes, 13 secondsलोड बैलेंस है यहां पे बना हुआ दिखाई देगा आपको सो आप सिंपली ये कमांड यूज़ करोगे इ एस सीटीएल गेट क्लस्टर सो आपको क्लस्टर का
9:12:219 hours, 12 minutes, 21 secondsनाम दिख जाएगा एंड उसके बाद सिंपल आप को यूज करना है ई एसीटीएल डिलीट क्लस्टर हाइन हान नेम एंड
9:12:309 hours, 12 minutes, 30 secondsउसके बाद आपके क्लस्टर का नाम जो कि है माय क्लस्टर सो आप देखोगे यहां पे आपका क्लस्टर डिलीट होना स्टार्ट हो जा रहा है
9:12:389 hours, 12 minutes, 38 secondsएंड एक बार जब आप ओवरऑल क्लस्टर ही डिलीट कर देते हो तो उसके अंदर के जो रिसोर्सेस है डिप्लॉयड है ऐप है सारी ही चीजें डिलीट
9:12:459 hours, 12 minutes, 45 secondsहो जाती है सो वो सारी चीजें कवर हो जाती है इसमें ओके देखोगे ऑल क्लस्टर रिसोर्सेस वर डिलीटेड चलो हम लोग यहां पे एक बार चेक
9:12:539 hours, 12 minutes, 53 secondsकर लेते हैं चलिए डैशबोर्ड पे आ गए एंड इसके बाद ईएस सर्विस पे चलते हैं एंड लेट्स सी सो आप देख रहे हो कि माय क्लस्टर
9:13:019 hours, 13 minutes, 1 secondडिलीट है ओके सो उसमें थोड़ा सा समय लग सकता है लद इसके अंदर के सारे रिसोर्सेस भी वह साथ में डिलीट कर देगा ओके सो इसके
9:13:109 hours, 13 minutes, 10 secondsबाद एंड में कुछ बेसिक चीजें मैं थोड़ी सी और बता देता हूं जो कि आपके लिए हेल्पफुल रहेगी अभी जो हम लोग ने नॉर्मली क्लस्टर बनाया था बहुत बेसिक बनाया था एंड
9:13:189 hours, 13 minutes, 18 secondsडायरेक्टली हम लोग ने इनेबल ऑटो मोड पे बनाया था ओके लेकिन आपने क्या देखा था जब ट इंस्टेंस इस मेथड से टू इंस्टेंस जो
9:13:279 hours, 13 minutes, 27 secondsबनाया वो x5 लार्ज समथिंग कुछ बनाया था ओके अगर आप उन चीजों को कंट्रोल करना चाहते हो राइट सो वो भी आप कर सकते हो
9:13:369 hours, 13 minutes, 36 secondsजैसे कि आप देखोगे काफी सारे ऑप्शंस यहां पे प्रोवाइड कर सकते हो जैसे कि नोड इक्वल 3 मतलब आप आपका एक ईटू इंस्टेंस बना था आप
9:13:449 hours, 13 minutes, 44 secondsयहां पे बोलते हो कि नहीं भैया मैं एक पे रिलाई नहीं कर सकता मेरा इंपॉर्टेंट एप्लीकेशन है कम से कम दो चाहिए तीन चाहिए दैट यू कैन डू तो यहां पे क्या नोट्स
9:13:539 hours, 13 minutes, 53 secondsइक्वल टू 3 कर दो ओके इसके बाद नोट्स मिनिमम और मैक्सिमम भी आप प्रोवाइड कर सकते हो ये चीज कहां पे यूज़ होगी ऑटो
9:14:019 hours, 14 minutes, 1 secondस्केलिंग में कि मान लो अब जैसे कि मिनिमम आपने दो रख दिया कि मतलब रिक्वेस्ट अगर कम आ रही है लोड कम है ट्रैफिक कम है तो वो
9:14:099 hours, 14 minutes, 9 secondsदो पे ले आएगा मिनिमम लेकिन दो रखेगा और मैक्सिमम फाइव अब ऐसा ना हो कि बहुत ट्रैफिक है तो कम से कम 50 इंस्टेंस बना
9:14:169 hours, 14 minutes, 16 secondsदें एंड आपका भैया बैंकर बल जाए तो आप मैक्सिमम ये इंस्टेंसस आप यहां पे प्रोवाइड कर सकते हो ठीक है ये रीजन आप
9:14:239 hours, 14 minutes, 23 secondsप्रोवाइड कर सकते हो एक स्पेसिफिक रीजन में अगर आपको बनाना है नोड टाइप यहां पर देखोगे नोड टाइप भी आप प्रोवाइड कर सकते हो जैसे कि t3 मीडियम सो दैट आपकी अगर
9:14:329 hours, 14 minutes, 32 secondsरिक्वायरमेंट कम है सिंपल एप्लीकेशन है सोम सो एंड जो कि हम लोग ने स्टार्टिंग में किया था कि जो आपके वर्कर नोट्स है वो
9:14:419 hours, 14 minutes, 41 secondsदो टाइप्स के है एक तो ी टू हो गया एक हो गया सर्वरलेस सो आप एक ये लास्ट में यह भी देख सकते हो हाइन हाइन फ गट सो फार गट
9:14:509 hours, 14 minutes, 50 secondsदेते हो तो सर्वरलेस बनता है वहां पे आपको c2 दिखाई नहीं देगा जो भी आपके एप्लीकेशन का मैनेजमेंट है रनिंग वह एड्स हैंडल
9:14:589 hours, 14 minutes, 58 secondsकरेगा ओके सो ऑटोमेटिक सब कुछ एडब्ल्यू के हाथ में जाएगा एंड जो क्लस्टर हम लोग ने
9:15:059 hours, 15 minutes, 5 secondsबनाया था जैसे कि ऑटो मोड पे उसके अलग-अलग टाइप्स होते हैं जैसे कि अभी हम लोग ने एक फार गट देखा जो कि सर्वरलेस मोड है इसी
9:15:139 hours, 15 minutes, 13 secondsतरीके से अलग-अलग टाइप के क्लस्टर्स भी होते हैं सो मैंने यहां पे कैटेगरी इज कर दिया है एंड किसके लिए सूटेबल बेस्ट सूटेबल होगा वो भी मैंने लिख दिया है एंड
9:15:219 hours, 15 minutes, 21 secondsमैनेज्ड बाय डब् कि एडब्ल्यू एस का कितना कंट्रोल होता है जैसे कि सिंपल है टू वर्कर नोट्स जो कि अभी हम लोग ने देखा था
9:15:289 hours, 15 minutes, 28 secondsकि ईसी टू इंस्टेंस वर्कर नोट्स इसने खुद बनाया था ओके तो एंड मैनेज बाय ईडब्ल्यूएस मतलब पार्शियली ईडब्ल्यूएस कंट्रोल करेगा एंड आप भी कर सकते हो क्योंकि ए2 दिख रहा
9:15:379 hours, 15 minutes, 37 secondsहै तो आप उसको एक्सेस भी कर सकते हो उसमें ठीक है मैनेज भी कर सकते हो फार गट मतलब टोटली सर्वरलेस होगा तो मैनेज बाय ईडब्ल्यूएस फुल्ली मैनेज्ड नोड ग्रुप्स अब
9:15:479 hours, 15 minutes, 47 secondsअपने खुद के नोट ग्रुप्स बना सकते हो मतलब वर्कर नोट्स के ग्रुप्स एंड वो भी मतलब एक तरीका है है आप खुद कर सकते हो फिर इसके
9:15:549 hours, 15 minutes, 54 secondsबाद ईएस एनी वेयर स्पॉट इंस्टेंसस आउटपोस्ट मल्टी क्लस्टर एंड सो ऑन सो अलग-अलग टाइप के सेटअप आप यहां पे कर सकते
9:16:019 hours, 16 minutes, 1 secondहो ऑलराइट सो दिस इज ऑल अबाउट दिस सेक्शन चलिए इस सेक्शन में समझते हैं

```
