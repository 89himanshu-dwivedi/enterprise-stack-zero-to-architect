# AWS Zero to Hero — Part 9 (Chapters 23–24)
## English Study Notes + Original Transcript Preservation

> **Coverage:** Chapter 23 — AWS Billing and Organization; Chapter 24 — AWS Amplify - Full Stack Web Demo  
> **Source policy:** Appendix A preserves the supplied transcript verbatim, including timestamps, speech-recognition spellings, repetitions, and sequence. Study notes and additional explanations are separate from the source transcript.

## Contents
1. Chapter 23 — AWS Billing and Organization
2. Chapter 24 — AWS Amplify: Full-Stack Web Demo
3. End-to-End Flow and Revision
4. Common Gotchas
5. Quick Revision Q&A
6. No-Skip Checklist
7. Board-Level Answer
8. Additional Points Added
9. Appendix A — Original Source, Preserved

---

# 1. Chapter 23 — AWS Billing and Organization

## 1.1 What is AWS Organizations?
AWS Organizations is a central management capability for managing multiple AWS accounts within one organization. The transcript uses the example of a team of five or six members, each with a separate AWS account, but all belonging to the same organization.

**Main idea:** instead of managing every account separately, use the organization for centralized account management, policy management, and billing visibility.

## 1.2 Core terms

| Term | Meaning |
|---|---|
| Organization | A logical group of multiple AWS accounts |
| Management account | The central account used to manage the organization; the transcript describes it as similar to an admin account |
| Member account | Another AWS account added or invited to the organization |
| Organizational Unit (OU) | A structure for grouping accounts |
| Service Control Policy (SCP) | A guardrail policy that sets the maximum available permissions for accounts/OUs |
| Invitation | A request for an existing account to join an organization |
| Consolidated billing | A model for centrally viewing/managing billing across organization accounts |

> **Important distinction:** an SCP does not itself grant IAM permissions. It defines a permission boundary/guardrail. Actual access is also governed by IAM policies and other applicable controls.

## 1.3 High-level organization structure

```text
AWS Organization
└── Root
    ├── Management Account
    ├── OU: Team / Environment (example)
    │   ├── Member Account A
    │   └── Member Account B
    └── Other Member Accounts
```

The transcript describes the management account as a master/admin-like account and introduces OUs as groups for accounts.

## 1.4 Two ways to add accounts
The console demonstration in the transcript describes two options:

1. **Create AWS account** — create a new account while setting up the organization.
2. **Invite existing account** — enter an existing account's email address or account ID and send an invitation. The account owner can accept or decline it.

**Flow:**
```text
Management account
      |
      +--> Create a new account
      |
      +--> Invite existing account --> Accept invitation --> Member account
```

## 1.5 Practical concept of SCPs
The transcript demonstrates enabling service control policies and creating a sample policy that denies S3 actions.

Conceptually:
- An SCP can be associated with an organization, OU, or account.
- A policy can restrict actions for a service.
- **It is incorrect to treat an SCP as a permission grant.** It restricts or sets guardrails around permissions.
- The management account is not restricted by SCPs in the same way as member accounts; SCPs are not used to restrict the management account.

Conceptual S3 deny flow:
```text
SCP policy
  └── Service: S3
      └── Selected actions: Deny
          └── Applicable member account/OU scope
```

## 1.6 AWS Pricing Calculator
The transcript presents AWS Pricing Calculator as a tool for estimating the cost of planned resource usage.

When creating an estimate:
- Select a service, such as EC2.
- Choose the Region/location.
- Select the resource configuration.
- Specify expected usage duration and relevant options.
- Review the estimated monthly cost for budget planning.

**Remember:** a calculator estimate is not a guarantee of the final bill. Actual cost can vary with usage, Region, storage, data transfer, discounts, pricing model, and configuration.

## 1.7 Billing and Cost Management dashboard
The transcript reviews these dashboard areas:
- Current month / month-to-date cost
- Previous month's cost
- Forecasted cost
- Bills and payment-related options
- Cost breakdown by AWS service
- Service-level analysis and reporting in Cost Explorer

### How to investigate an unexpected bill
1. Identify the cost spike in the billing dashboard.
2. Review service-level spending in the cost breakdown / Cost Explorer.
3. If EC2 is high, inspect running instances, forgotten test resources, storage, and usage configuration.
4. Verify resources associated with the relevant service.
5. Safely stop or delete unneeded resources after checking data-retention requirements.
6. Consider configuring budgets and alerts.

```text
Unexpected bill
   ↓
Billing dashboard
   ↓
Cost breakdown / Cost Explorer
   ↓
Identify high-cost service
   ↓
Investigate individual resources
   ↓
Right-size, stop, delete, or adjust
```

## 1.8 Chapter 23 — key takeaways
- Organizations helps centrally manage multiple AWS accounts.
- OUs can group accounts logically.
- SCPs are guardrails, not direct permission grants.
- Pricing Calculator estimates planned usage costs.
- The billing dashboard and Cost Explorer help inspect actual costs.
- Cleaning up lab resources is an important cost-control practice.

---

# 2. Chapter 24 — AWS Amplify: Full-Stack Web Demo

## 2.1 Demo goal
The transcript describes a practical walkthrough for deploying a static website and a React-based To-Do app through Amplify. It mentions custom domains, using Route 53 or a third-party domain provider, certificates, frontend-backend integration, and AWS resources.

For the React To-Do example, the transcript refers to components such as DynamoDB, Lambda, and Cognito. The later part of the demo adds user authorization so that each logged-in user sees their own To-Do list.

## 2.2 Role of Amplify
In this demonstration, Amplify is used to simplify development and deployment workflows:
- Deploying an app from a Git repository
- Hosting a static site or React app
- Setting up a custom domain
- Integrating HTTPS certificates and domains
- Connecting frontend and backend resources
- Supporting local development and a sandbox workflow

> This section organizes the demonstration described in the supplied transcript. Exact console screens and commands can depend on project configuration and the Amplify tooling version.

## 2.3 Main stages of the demo

### Stage A — Deploy a static website
1. Keep the website code in a Git repository.
2. Create/connect an app in the Amplify console.
3. Select the repository and branch.
4. Let the build/deployment process complete.
5. Verify the site at its generated hosted URL.

### Stage B — Custom domain and certificate
The transcript plans to demonstrate using a custom domain with a hosting provider or Route 53 and reviewing the certificate workflow.

High-level DNS flow:
```text
Custom domain
    ↓ DNS records
Amplify-hosted app
    ↓
HTTPS certificate / secure browser connection
```

DNS records must point to the correct target, and certificate validation must complete. Use the exact record names and values shown in the live console instructions.

### Stage C — React To-Do app and backend
The transcript describes setting up a React sample app locally, changing frontend/backend code, and connecting resources. Mentioned services include DynamoDB, Lambda, and Cognito.

Conceptual architecture:
```text
Browser / React frontend
        |
        | Authenticated app requests
        v
Amplify-connected backend / API
        |
        +--> Lambda function (business logic)
        |
        +--> DynamoDB (application data)
        |
        +--> Cognito / user pool (user identity and sign-in)
```

This diagram is conceptual; the exact API/service wiring depends on the app implementation.

## 2.4 Per-user To-Do list — authorization change
The transcript's backend change aims to give every user an account and a separate To-Do list.

In this flow, the transcript:
- Changes a data resource/type file under Amplify.
- Describes changing the default authorization mode from API key to user-pool authorization.
- Adds/updates user and sign-out UI in the frontend.
- Runs the app locally using `npm run dev`.
- Creates an account again in the new sandbox environment and verifies the per-user list.

**Concept:** authentication identifies the user; authorization and data-access rules decide which data the user may read or write. Merely displaying a sign-in button does not by itself guarantee secure per-user data isolation — backend authorization/schema rules must also be correct.

## 2.5 Sandbox workflow
The transcript describes a sandbox as a separate development/testing environment that is isolated from the main backend/database, allowing changes to be tested.

```text
Main / deployed environment
          |
          | Separate sandbox resources
          v
Sandbox environment
   ├── Test schema/config changes
   ├── Test user authorization
   └── Verify behavior without changing main data
```

The transcript mentions the `npx ampx sandbox` command and says that after sandbox setup, the generated `amplify_outputs` file is updated/overridden. Verify exact commands and output-file behavior against the documentation for the Amplify tooling version installed in your project.

### Sandbox safety checklist
- Confirm that the app is using the intended sandbox output/configuration.
- Avoid accidental connections to the main/production backend.
- Use test users and test data in the sandbox.
- Clearly identify resource names and environments.
- Delete sandbox resources when finished.

## 2.6 Code changes, Git push, and deployment
The transcript demonstrates adding, committing, and pushing code changes with Git. Its explanation says the sandbox-generated output file was not being committed, and the hosted app was redeployed after code was pushed.

Generic workflow:
```text
Edit code
   ↓
Run locally / test sandbox
   ↓
git add ...
   ↓
git commit ...
   ↓
git push
   ↓
Hosting build & deployment
   ↓
Verify deployed app
```

Whether a Git push triggers a build depends on the repository/branch and the Amplify app configuration.

## 2.7 Cleanup — a critical part of the demo
At the end, the transcript cleans up resources:
- Deletes the sandbox.
- Deletes the Amplify static website app.
- Deletes the Amplify React app.
- Checks DynamoDB resources/tables.
- Cleans up the Route 53 hosted zone and records; it mentions deleting the Amplify-generated CNAME record first.
- States that the domain registration itself is retained separately.

**Cost and safety tip:** do not assume deleting an app always deletes every associated resource in every situation. Individually verify DynamoDB, Lambda, Cognito, DNS records, hosted zones, logs, storage, certificates, and other resources. Deleting a domain registration and deleting a hosted zone are different actions.

## 2.8 Chapter 24 — key takeaways
- Amplify is used in workflows for deploying and managing static websites and full-stack frontend apps.
- Custom domains require DNS records and certificate setup to be verified.
- A React frontend can be integrated with backend resources and user authentication.
- Per-user data isolation requires correct backend authorization/data rules in addition to authentication.
- Sandbox testing can help validate development changes without affecting main data, provided the configuration is correct.
- A Git push can trigger deployment depending on the app pipeline settings.
- Verifying cleanup after the demo helps avoid unwanted charges.

---

# 3. End-to-End Flow and Revision

## 3.1 Billing and Organizations
```text
Multiple AWS accounts
       ↓
AWS Organizations
       ↓
Management account + OUs + member accounts
       ↓
Policies / SCP guardrails
       ↓
Central billing visibility
       ↓
Pricing estimate + Cost Explorer review
```

## 3.2 Amplify full-stack deployment
```text
Git repository
    ↓
Amplify hosting / build
    ↓
Static website or React frontend
    ↓
Custom domain + DNS + HTTPS
    ↓
Backend integration
    ├── Auth / user pool
    ├── API / Lambda
    └── DynamoDB
    ↓
Test sandbox → verify deployment → cleanup
```

## 3.3 Quick comparison

| Need | Relevant feature/tool in the transcript |
|---|---|
| Centrally manage multiple AWS accounts | AWS Organizations |
| Account/OU-level permission guardrails | SCP |
| Estimate planned AWS costs | AWS Pricing Calculator |
| Inspect current/previous/forecasted spending | Billing dashboard |
| Analyze spending by service | Cost Explorer |
| Host and deploy a website/app | AWS Amplify |
| Map a domain | Custom domain + DNS/Route 53 or provider |
| User sign-in | Cognito/user pool, as described in the demo |
| Separate test environment | Amplify sandbox workflow |
| Avoid unexpected charges | Resource inventory and cleanup |

---

# 4. Common Gotchas

1. **SCP ≠ IAM grant:** an SCP can provide allow-list/deny guardrails, but does not itself grant a user permission.
2. **Estimate ≠ final bill:** Pricing Calculator is a planning estimate; actual usage may differ.
3. **Investigate bill spikes by service:** do not guess from the total bill; start with Cost Explorer/breakdown.
4. **Authentication ≠ authorization:** signing in does not automatically isolate data.
5. **Sandbox configuration mismatch:** an app may accidentally use production/main output or configuration; verify the environment.
6. **DNS propagation/certificate validation:** verify records and certificate status during custom-domain setup.
7. **Git push behavior depends on the pipeline:** check the correct repository/branch and build settings.
8. **Deleting an app does not guarantee total cleanup:** verify related resources in the console.
9. **Hosted zone vs domain registration:** deleting a hosted zone is not the same as cancelling a domain registration.
10. **Test resources can still cost money:** inventory leftover resources after the demo.

---

# 5. Quick Revision Q&A

**Q1. What is the main purpose of AWS Organizations?**  
To organize and centrally manage multiple AWS accounts, apply policies, and simplify billing management.

**Q2. What is an OU?**  
A structure for logically grouping accounts within an organization.

**Q3. What does an SCP do?**  
It places guardrails on permissions for accounts/OUs. It does not grant permissions by itself.

**Q4. What is AWS Pricing Calculator used for?**  
Estimating the cost of planned resource usage.

**Q5. Where can actual costs be inspected by service?**  
In the Billing and Cost Management dashboard and Cost Explorer.

**Q6. What was deployed in the Amplify demo?**  
The transcript describes deploying a static website and a React-based To-Do app.

**Q7. What matters for a per-user To-Do list?**  
Authentication plus backend schema/authorization rules that limit users to the records they are allowed to access.

**Q8. What was the sandbox used for?**  
Testing changes and authorization behavior without affecting the main backend/data.

**Q9. What should be checked during custom-domain setup?**  
DNS records, target mapping, certificate validation, and HTTPS behavior.

**Q10. Why clean up after the demo?**  
To remove/verify unneeded resources and avoid unexpected charges or leftover infrastructure.

---

# 6. No-Skip Checklist

- [ ] AWS Organizations multi-account use case
- [ ] Central management/admin concept
- [ ] OUs and member accounts
- [ ] Creating a new account
- [ ] Inviting an existing account and accepting/declining
- [ ] Enabling/creating/attaching an SCP and the S3 deny example
- [ ] AWS Pricing Calculator: service, Region, configuration, duration, estimate
- [ ] Billing dashboard: current, previous, forecast, bills/payment
- [ ] Cost breakdown and Cost Explorer
- [ ] Investigating high EC2 costs
- [ ] Amplify static website deployment
- [ ] Custom domain, DNS/Route 53, and certificates
- [ ] React To-Do app deployment
- [ ] DynamoDB, Lambda, and Cognito/user-pool references
- [ ] Per-user authorization changes
- [ ] Local `npm run dev` flow
- [ ] `npx ampx sandbox` sandbox flow
- [ ] Sandbox output/config file change
- [ ] Git add/commit/push and deployment verification
- [ ] Sandbox, apps, data resources, and hosted-zone cleanup
- [ ] Difference between hosted zone and domain registration

---

# 7. Board-Level Answer

**AWS Organizations** is a multi-account management service that helps centrally manage multiple AWS accounts within an organization. Accounts can be grouped into OUs, and SCPs can define permission guardrails. The billing dashboard and Cost Explorer help inspect service-level spending and trends, while AWS Pricing Calculator estimates the cost of a planned architecture.

**AWS Amplify** is used in this demo for web app hosting and full-stack development/deployment workflows. A static website and React To-Do app can be deployed through a Git-based workflow; a custom domain, DNS, and certificates can be configured; and the frontend can connect to backend components such as authentication, API/Lambda, and DynamoDB. Correct backend authorization rules and isolated sandbox testing are important for per-user data access. After the demo, cleanup and verification of the app and related resources are essential cost-control steps.

---

# 8. Additional Points Added

> **These are assistant-added explanations; they are not verbatim parts of the original transcript.**

- Organizing production accounts into separate environments (development, test, production) can reduce operational risk.
- Use SCPs as least-privilege governance guardrails and review actual user/role permissions through IAM policies as well.
- Proactively configuring billing alerts/budgets is useful, but an alert does not automatically stop resource spending.
- Before custom-domain changes, record existing DNS records and follow TTL/validation instructions.
- Enforce user-level data isolation on the backend; do not rely only on frontend UI filtering.
- Before cleanup, review the deletion impact on logs, databases, domains/DNS, and other retained assets.
- Commands and console labels can change across tooling versions; follow the official documentation for the installed version in a live project.

---

# 9. Appendix A — Original Source, Preserved

The supplied transcript for Chapters 23 and 24 is preserved below in its original sequence. Speech-recognition spellings, timestamps, and wording have intentionally not been normalized or corrected.

```text
Chapter 23: AWS Billing and Organization
7:39:257 hours, 39 minutes, 25 secondsकरिए चलिए इस सेक्शन में बात करते हैं एडब्ल्यू एस ऑर्गेनाइजेशन में ठीक है तो एडब्ल्यू एस ऑर्गेनाइजेशन इस पर मैं क्लिक
7:39:327 hours, 39 minutes, 32 secondsकर लेता हूं एंड इसका सिंपल सा एक यूज केस है कि मान लीजिए अगर आपकी टीम में लेट्स
7:39:397 hours, 39 minutes, 39 secondsसे फाइव सिक्स मेंबर्स हैं इनके अलग-अलग एडब्ल्यू एस अकाउंट्स बने हुए हैं ठीक है बट विदन एक आप एक ही ऑर्गेनाइजेशन के ठीक
7:39:477 hours, 39 minutes, 47 secondsहै सारे मेंबर्स एक ही ऑर्गेनाइजेशन के पार्ट हैं राइट क्योंकि आप एक टीम के मेंबर हो सो अगर बिलिंग की बात करें या तो
7:39:547 hours, 39 minutes, 54 secondsसारे अकाउंट्स को मैनेज करने की बात करें सो इट बिकम वेरी इजी कि आप ऑर्गेनाइजेशन क्रिएट करो एंड सारे एडब्ल्यू अकाउंट को
7:40:037 hours, 40 minutes, 3 secondsएक मतलब जगह आप मैनेज कर सकते हो एंड ऑफकोर्स इसमें बिलिंग हो गई फाइनेंस हो गया ये सारी चीजों को हैंडल करने में काफी ज्यादा आसान होता है क्योंकि आपको एक ही
7:40:117 hours, 40 minutes, 11 secondsजगह करना है ये देखो सेंट्रल मैनेजमेंट फॉर मल्टीपल एडब्ल्यू अकाउंट तो सेंट्रल मैनेजमेंट है मान लो कोई भी एक आप एडमिन
7:40:187 hours, 40 minutes, 18 secondsहै रहता है आपके पास सो वो एक पर्सन बाकी सारे एडब अकाउंट्स को मैनेज कर सकता है एंड उनको क्या परमिश है पॉलिसीज वगैरह भी
7:40:267 hours, 40 minutes, 26 secondsआप क्रिएट कर सकते हो जैसे कि देखोगे हाउ इट वर्क्स आप सिंपल ऑर्गेनाइजेशन बना सकते हो फिर जो डिफरेंट एडब्ल्यूएल काउंट्स है
7:40:357 hours, 40 minutes, 35 secondsसिंपल ऐड करके आप उनको अपने ऑर्गेनाइजेशन में ऐड कर सकते हो लाइक दिस तो यहां पे आपको देखोगे एक मास्टर या तो एडमिन होगा
7:40:437 hours, 40 minutes, 43 secondsबाकी दूसरे एडब्ल्यूएल काउंट्स होंगे ग्रुप अकाउंट जिसमें एंड इसको हम लोग ऑर्गेनाइजेशन यूनिट ओ यूज भी बोलते हैं
7:40:507 hours, 40 minutes, 50 secondsओके एंड अप्लाई पॉलिसीज ऑफकोर्स कि आपको ठीक है तो आप एक ही जगह से सारे ही डिफरेंट अकाउंट्स में ये चीजें अप्लाई कर
7:40:587 hours, 40 minutes, 58 secondsसकते हो जिसको हम बोलते हैं सर्विस कंट्रोल पॉलिसीज एससीपी एंड आप किसको क्या अप्लाई करना है नहीं करना वह चीज भी आप कंट्रोल
7:41:067 hours, 41 minutes, 6 secondsकर पाओगे इनेबल ईडब्ल्यूएस सर्विसेस एंड ऑल सो देयर इज नो कॉस्ट टू यूज ईडब्ल्यूएस ऑर्गेनाइजेशन सो दिस इज कॉस्ट फ्री ओके सो
7:41:147 hours, 41 minutes, 14 secondsयह एडिशनल सर्विस है और यह ग्लोबली मतलब प्रेजेंट है सो ऑफकोर्स क्योंकि ये सिर्फ आपके अकाउंट मैनेजमेंट के लिए है ठीक है
7:41:237 hours, 41 minutes, 23 secondsसो सिंपल सा एक चीज है और यहां पे एडब्ल्यू अकाउंट पे जाते हैं सो यहां पे जब ऑर्गेनाइजेशन आप क्लिक करते हो यू नो क्रिएट ऑर्गेनाइजेशन तो आप देखोगे
7:41:317 hours, 41 minutes, 31 secondsएडब्ल्यू अकाउंट सो अभी जो मेरा मेन अकाउंट है पल फप जो कि मेरा एडमन अकाउंट है सो आप देखोगे ऑर्गेनाइजेशन एक बनी हुई है एक रूट अकाउंट जिसमें मेरा यह वाला
7:41:407 hours, 41 minutes, 40 secondsअकाउंट है पल फिप मैनेजमेंट अकाउंट सो मतलब यह मेरा मैनेजमेंट अकाउंट है सो मतलब यह काइंड ऑफ एडमिन आप समझ सकते हो ठीक है
7:41:477 hours, 41 minutes, 47 secondsअब हायर की लिस्ट हो गया अब एक्शन अ कर सकते हो एडब्ल्यू अकाउंट्स ठीक है एंड इसमें यहां पे दो चीजें हैं देखो ऐड एन
7:41:547 hours, 41 minutes, 54 secondsएडब्ल्यू अकाउंट तो या तो आप एक नया अकाउंट मतलब देखो आप जैसे कि स्टार्टिंग से कर रहे हो बिल्कुल स्क्रैच से सारी बिल्डिंग या तो ऑर्गेनाइजेशन सेटअप कर रहे
7:42:027 hours, 42 minutes, 2 secondsहो तो आप एडब्ल्यू अकाउंट क्रिएट करके आप ठीक है प्रोवाइड कर सकते हो या तो इनवाइट एंड एजिस्टिफाई
7:42:137 hours, 42 minutes, 13 secondsमेेल आप एंटर कर दो ठीक है या तो आप इनका क्या है अकाउंट आईडी भी डाल सकते हो एंड सिंपल क्या करेगा वो इनविटेशन एक भेज देगा
7:42:217 hours, 42 minutes, 21 secondsअब इन्विटेशन कहां आएगा अगर आप इसी में अगर मैं दिखाऊं सो आप देखोगे एडब्ल्यू अकाउंट लेफ्ट साइड में और इनविटेशन सो सी
7:42:287 hours, 42 minutes, 28 secondsदेयर इज नो टेशन सो आपके केस में लेट्स से एक टीम मेंबर है उसका डब्लू अकाउंट है वो
7:42:347 hours, 42 minutes, 34 secondsइनविटेशन में जाके चेक करेगा तो इनवाइट एक्सेप्ट करने का या तो डिक्लाइन कर रहेगा एक्सेप्ट करेगा तो वो आपके ऑर्गेनाइजेशन
7:42:427 hours, 42 minutes, 42 secondsमें जुड़ जाएगा ओके वेरी सिंपल एंड आल्सो पॉलिसीज आप पॉलिसीज यहां पे कर सकते हो अब जैसे कि यहां पे देखोगे काफी सारे अलग-अलग
7:42:497 hours, 42 minutes, 49 secondsटाइप के जैसे कि सर्विस कंट्रोल पॉलिसी जो काफी है इनेबल सर्विस कंट्रोल पॉलिसीज ओके सो लेट्स से मैंने इसको इनेबल कर दिया ठीक
7:42:577 hours, 42 minutes, 57 secondsहै अभी यहां पे देखो देखोगे सिर्फ सिर्फ एक जो डिफॉल्ट पॉलिसी आप देख सकते हो फुल एडब्ल्यू एक्सेस ऑफकोर्स फॉर द एडमिन इट
7:43:057 hours, 43 minutes, 5 secondsइज ठीक है इसके साथ-साथ आप पॉलिसीज अटैच मतलब बना के अटैच भी कर सकते हो जैसे कि क्रिएट पॉलिसी की जैसे कि मैं एक पॉलिसी
7:43:137 hours, 43 minutes, 13 secondsबना लेता हूं नाई s3 कि मतलब s3 के लिए मुझे डिनायर है एंड यहां पे आप इस तरह से रूल डिफाइन कर सकते हो जैसे कि फिल्टर
7:43:217 hours, 43 minutes, 21 secondsसर्विस तो मैं सिंपल यहां पे s3 करता हूं ओके s3 एंड यहां पे देखोगे ऑल एक्शंस ठीक है s3 से रिलेटेड सारे ही एक्शन में आपको
7:43:307 hours, 43 minutes, 30 secondsक्या करना है यह डिनायर है ओके
7:43:457 hours, 43 minutes, 45 secondsदैट्ची बना सकते हो अटैच कर सकते हो सो बेसिकली आपको एक ओवरव्यू मिल गया होगा कि आप एडब्ल्यू ऑर्गेनाइजेशन से कैसे डिफरेंट
7:43:547 hours, 43 minutes, 54 secondsईडब्ल्यूएस अकाउंट्स ठीक है आप हैंडल कर सकते हो ओके सो इसके बाद बात करते हैं एडब्ल्यू प्राइसिंग कैलकुलेटर बिकॉज जब भी
7:44:027 hours, 44 minutes, 2 secondsहम एडब्ल्यू रिसोर्सेस को यूज करने के बारे में सोचते हैं सबसे पहले क्वेश्चन हमेशा आता है दिमाग में कि भाई कितना कॉस्ट होगा या तो कितना हमको जेब पे भारी
7:44:107 hours, 44 minutes, 10 secondsपड़ेगा ऑफकोर्स क्योंकि कंपनीज में भी ऐसा नहीं है कि अनलिमिटेड पैसा है उनका भी बजट होता है सो यह आपकी बड़ी हेल्प करेगा कैलकुलेटर एए इनकी एक वेबसाइट है जहां पे
7:44:197 hours, 44 minutes, 19 secondsआप एक एस्टीमेट निकाल सकते हो डिपेंड अपॉन कि आप क्या यूज करना चाहते हो क्या चीजें प्लान कर रहे हो जैसे कि क्रिएट एस्टीमेट
7:44:277 hours, 44 minutes, 27 secondsकिया ओके बाय लोकेशन टा टाइप बाय ऑल सर्विसेस जैसे कि रीजन हो गया एंड ऑल ओके फाइंड द सर्विस अब लेट्स से मैं बोलता हूं
7:44:347 hours, 44 minutes, 34 secondsकि e2 ठीक है चाहिए सो e2 में देर सो मेनी ऑप्शन ये सिंपल e2 की बात करें तो कॉन्फिन
7:44:417 hours, 44 minutes, 41 secondsअब e2 में किस टाइप की आप मतलब जो रिसोर्सेस है यूज करने की सोच रहे हो जैसे कि
7:44:597 hours, 44 minutes, 59 secondsटाइप आप क्या यूज़ कर रहे हो एंड सो मेनी थिंग्स मतलब आप यहां पे देख सकते हो कितने-कितने टाइम तक के लिए आप प्लान कर
7:45:067 hours, 45 minutes, 6 secondsरहे हो एंड इसमें आपको एक अ एक ऑप्शन मिल जाएगा मतलब एक आईडिया मिल जाएगा कि आपको कितना यहां पे कॉस्ट मिलेगा लाइक जैसे कि
7:45:147 hours, 45 minutes, 14 secondsमंथली इतना इतना डॉलर्स अ पर अ यू नो पर मंथ आएगा एंड इसके बाद बात करते हैं कि
7:45:217 hours, 45 minutes, 21 secondsअभी करेंटली हमारी क्या प्राइसिंग और कितना हमारा खर्चा हो रहा है डेली बेसिस पे मंथली बेसिस पे सो वो चीज हमको यहां पर
7:45:297 hours, 45 minutes, 29 secondsडैशबोर्ड पे दिख जाता है ऑन द यूजर बेसिस जैसे कि कॉस्ट एंड यूसेज और और ये चीज को आप अच्छे से और देख सकते हो जो कि एक
7:45:377 hours, 45 minutes, 37 secondsबिलिंग एंड कॉस्ट मैनेजमेंट इनका एक जो डैशबोर्ड है इसमें ठीक है तो मैं इसमें क्लिक करता हूं सो इसमें बहुत ही अच्छे से पूरा ठीक है यहां पे डिफाइन रहता है कि
7:45:467 hours, 45 minutes, 46 secondsलास्ट मंथ कितना था अभी अ टिल डेट या इस मंथ में कितना है ये फोरकास्ट मतलब इस महीने का कितना बजट जा सकता है बिल बिल्स
7:45:567 hours, 45 minutes, 56 secondsवगैरह आप चेक कर सकते हो यहां से आप बिल पे पेमेंट वगैरह कर सकते हो ओके एंड यहां पे सारी आपकी पेमेंट एंड कार्ड्स रिलेटेड
7:46:037 hours, 46 minutes, 3 secondsऑफ यू नो ऑप्शंस है एंड कॉस्ट ब्रेकडाउन की अगर बात करें कि जैसे कि फॉर एग्जांपल यू आर नॉट एक्सपेक्टिंग कि भाई ये 2 पॉट
7:46:117 hours, 46 minutes, 11 secondsसमथिंग डॉलर्स कहां से आए तो ये जो कॉस्ट ब्रेकडाउन है इसमें बहुत अच्छे से जैसे कि आप इसपे सिर्फ होवर करोगे आपको दिख जाएगा कि कौन सी सर्विसेस आपका कितना पैसा मतलब
7:46:197 hours, 46 minutes, 19 secondsयू नो चार्ज हो रहा है जैसे कि e2 में सबसे ज्यादा लग रहा है एंड ऑल सो मेनी रीजन सो दिस इज आल्सो वेरी गुड थिंग जैसे
7:46:267 hours, 46 minutes, 26 secondsकि अगर आपको एकदम से कभी दिखता है कि अरे कि य बिल बहुत ज्यादा आ गया है आप यहां से ब्रेकडाउन में देख सकते हो जैसे कि अगर आपको दिखता है e2 तो आप सिंपली ट के
7:46:347 hours, 46 minutes, 34 secondsडैशबोर्ड पर जाकर देख सकते हो कि आपकी कोई इंस्टेंस छूट तो नहीं गए हैं या तो एक प्रॉपर्ली कॉन्फिनेट रिसोर्सेस तो यूज नहीं कर लिए
7:46:447 hours, 46 minutes, 44 secondsहैं सो मेनी थिंग्स तो यहां पर आप यह चीजें में मैनेज कर सकते हो एनालाइज योर कॉस्ट इन कॉस्ट एक्सप्लोरर ठीक है तो मैं सिंपली यहां पर क्लिक करता
7:46:527 hours, 46 minutes, 52 secondsहूं सो यहां पर सर्विस काउंट 19 है ठीक है एंड जिसमें आप देख सकते हो ठीक है यह देखो
7:47:007 hours, 47 minutesयह पूरा बढ़िया अच्छे से ब्रेक डाउन करके यहां पे आपको दिखा देता है ओवरऑल एंड यहां पे आप रिपोर्ट भी जनरेट कर सकते हो अब
7:47:087 hours, 47 minutes, 8 secondsयहां पे मैंने देखो 19 सर्विस यूज करी है सो आप यहां पे वो भी देख सकते हो सर्विस टोटल ठीक है किस-किस चीज का क्या-क्या है
7:47:167 hours, 47 minutes, 16 secondsसो दिस इज आल्सो यूजफुल ऑल राइट सो चलिए सेक्शन में बात करेंगे एडब्ल्यू एमप्लीफाई सर्विस के बारे
Chapter 24: AWS Amplify - Full Stack Web Demo
7:47:247 hours, 47 minutes, 24 secondsमें हम लोग जन टू या तो नेक्स्ट जनरेशन सर्विस हम लोग इसमें कवर करने वाले वाले हैं इस वीडियो में हम लोग प्रैक्टिकल करके देखेंगे एक स्टैटिक वेबसाइट जो कि गब में
7:47:337 hours, 47 minutes, 33 secondsअगर प्रेजेंट है तो एंपलीफायर की मदद से कैसे डिप्लॉयडोकस इसका कैसे वर्क करता है कस्टम डोमेन हम लोग उसको ऐड करके देखेंगे
7:47:417 hours, 47 minutes, 41 secondsहोस्टिंगर या तो आपका जो राउट 53 है दोनों से ही हम लोग यूज कर सकते हैं सर्टिफिकेट इसमें कैसे काम करते हैं इसके बाद हम लोग
7:47:487 hours, 47 minutes, 48 secondsएक रिएक्ट बेस्ड टू डू सैंपल एक ऐप भी हम लोग डिप्लॉयड उस रिएक्ट एप्लीकेशन को आपको
7:47:557 hours, 47 minutes, 55 secondsलोकल एनवायरमेंट में कैसे सेटअप करना है उसमें फ्रंट एंड बैक एंड में कैसे चेंजेज करना है कैसे अलग-अलग रिसोर्सेस को मिला
7:48:027 hours, 48 minutes, 2 secondsके एक ऐप बनता है जैसे कि फुल स्टैक वेब अ एप्लीकेशन इंक्लूडिंग डायनेमो डीबी लडा फंक्शन कॉग्निटो एंड सो मेनी थिंग्स
7:48:107 hours, 48 minutes, 10 secondsअच्छी-अच्छी फंक्शंस फीचर्स एंड इसके बाद एंपलीफायर को अगेन अ क्लाउड सैंडबॉक्स जो एक और कांसेप्ट है वो कैसे वर्क करता है
7:48:187 hours, 48 minutes, 18 secondsएंड सो ऑन सो काफी प्रैक्टिकल बेस्ड एप्ल मतलब ये वीडियो होने वाला है एंड इसके अलावा चलो अगर स्टार्टिंग में सबसे पहले ही सोच रहे हो कि प्राइसिंग क्या क्या सीन
7:48:277 hours, 48 minutes, 27 secondsहै तो एमप्लीफाई का सो मैं प्राइसिंग आपको यहां पे दिखा दे रहा हूं यूज केस एंड यहां पे फ्री टियर इसमें अवेलेबल है फ्री टीआर
7:48:347 hours, 48 minutes, 34 secondsमें आप देख सकते हो बिल्डंग डिप्लॉयड डटा स्टोरेज डाटा ट्रांसफर सो अ बैक एंड एंड सो मेनी थिंग्स ठीक है तो ये आपको काफी
7:48:427 hours, 48 minutes, 42 secondsसारी चीजें यहां पे फ्री टियर में अवेलेबल है सो आप एक बार देख सकते हो ठीक है इसको गो थ्रू कर सकते हो इन शॉर्ट एमप्लीफाई
7:48:507 hours, 48 minutes, 50 secondsक्या करता है यह एक प्लेटफार्म है जो आपको हेल्प करता है तो सिंपलीफाई करता है बिल्डिंग डिप्लॉयड एंड होस्टिंग ऑफ योर
7:48:577 hours, 48 minutes, 57 secondsवेबसाइट मतलब जिसमें आप फुल स्टैक वेब ऐप भी हो सकते हैं या तो मोबाइल एप्लीकेशंस भी हो सकती
7:49:057 hours, 49 minutes, 5 secondsहैं इसको डायरेक्टली हम लोग प्रैक्टिकल करके समझते हैं तो आपको ज्यादा रिलेटेबल होगा ये हमारा हो गया एडब्ल्यू ए का कंसोल
7:49:137 hours, 49 minutes, 13 secondsजहां पे हम लोग एडब्ल्यू सर्विस ठीक है बनाएंगे यूज करेंगे एंड इसके बाजू में मेरे पास है गिट हब जिसमें मैंने ऑलरेडी
7:49:197 hours, 49 minutes, 19 secondsलॉग इन करके रखा है गिट हब में लेट्स से मेरे पास एक वेबसाइट ऑलरेडी बनी हुई है वेबसाइट तो नहीं है एक सिंपल सा स्टैटिक
7:49:277 hours, 49 minutes, 27 secondsवेब पेज ही है जो मैं आपको दिखा देता हूं माय रेपो अ एक रिपोजिटरी है जो कि पब्लिक रेपो है एंड इस पब्लिक रेपो में आप देखोगे
7:49:347 hours, 49 minutes, 34 secondsबहुत ही सिंपल index.htm एंड स्ल्ड
7:49:587 hours, 49 minutes, 58 secondsराइट अब मैं आपको दिखाता हूं कि एंपलीफायर की मदद से आप यह सारी चीजें कितने इजली सॉर्ट आउट कर सकते हो चलो मैं दिखाता हूं
7:50:057 hours, 50 minutes, 5 secondsये लेट्स से हमारी वेबसाइट रेडी है इसके बाद हम लोग चलते हैं एडब्ल्यू एस के कंसोल पे सिंपली हम लोग जाएंगे एडब्ल्यू एस एलीफ
7:50:137 hours, 50 minutes, 13 secondsके के पेज पे ठीक है इसके बाद मैं क्या करता हूं डिप्लॉयड ऐप मतलब क्योंकि हमारी वेबसाइट तो रेडी है तो हम लोग को सिर्फ
7:50:217 hours, 50 minutes, 21 secondsडिप्लॉयड साइट लेके आनी मैं बोलता हूं गेट हप क्योंकि गेट हब पे हमारा वेबसाइट है चलिए ठीक है इसके बाद नेक्स्ट करते हैं बाकी आप आपके पास अलग-अलग ऑप्शंस भी है
7:50:297 hours, 50 minutes, 29 secondsबिट बकेट कोड कमिट गट लैब या तो विदाउट गट क्योंकि मान लो आपके s3 पे अगर ये फाइल्स
7:50:377 hours, 50 minutes, 37 secondsरखी हुई है अपलोड करके तो वहां से भी आप यूज़ कर सकते हो और गिट हब हम लोग ने सेलेक्ट किया नेक्स्ट पे करते हैं एंड जब आप फर्स्ट टाइम आप करोगे ठीक है तो आपको
7:50:467 hours, 50 minutes, 46 secondsयह परमिशन पूछेगा क्योंकि देखो ऑलरेडी लॉक्ड इन था अपन को परमिशन देना है कि वो रेपो में कुछ चेंजेज कर सकते हैं कि नहीं देखो ऑल रेपो है ओनली सिलेक्टेड रेपो इट्स
7:50:557 hours, 50 minutes, 55 secondsऑल अप टू यू जैसे कि मैं ओनली सिलेक्टेड रेपो करता हूं एंड इसमें क्या चाहिए था मुझे माय रेपो ठीक है सेलेक्ट किया इंस्टॉल एंड ऑथराइज वेरी सिंपल ठीक है तो
7:51:047 hours, 51 minutes, 4 secondsपासवर्ड मांगेगा आई कैन प्रोवाइड द पासवर्ड तो ये अपने लॉक्ड इन और ऑथराइजेशन हो गया सो ये अभी एडब्ल्यू ए कंसोल हमारा
7:51:117 hours, 51 minutes, 11 secondsगिट हब से कनेक्टेड हो गया हम लोग ने अपनी रिपोजिटरी का एक परमिशन दे दी अब इसमें देखना है यह रिफ्रेश करो सिंपल और यहां पे
7:51:197 hours, 51 minutes, 19 secondsजैसे ही करोगे आपकी जो रेपो है वो यहां पे दिख जाएगी ठीक है इसको सेलेक्ट कर लो एंड सिंपल नेक्स्ट इसके बाद ऐप का क्या नाम
7:51:267 hours, 51 minutes, 26 secondsरखना है तो माय स्टैटिक वेबसाइट ऐसा नाम रख देता हूं बाकी ठीक ठीक है बाकी चीजें नेक्स्ट कर देते हैं ठीक है एक बार आप रिव्यू कर लो चीजें लद इट्स अ वेरी
7:51:357 hours, 51 minutes, 35 secondsस्टैटिक वेबसाइट तो कुछ है नहीं सेव एंड डिप्लॉयड ओके सो एक बार डिप्लॉयड कंप्लीट हो जाएगा यहां पे डिप्लॉयड साइन आ जाएगा देखो टिक मार्क भी आ रहा है एंड उसके बाद
7:51:447 hours, 51 minutes, 44 secondsजैसे ही आप इस डोमेन को क्लिक करते हो सो आपको यह वेबसाइट दिख जाएगी मतलब जो भी आपका वेब पेज है तो ये मेरा वेब पेज है
7:51:517 hours, 51 minutes, 51 secondsदिस इज अ डिजाइन वर्जन v7 वेलकम टू गब सो इट्स सक्सेसफुली ओके सो गब पे आपकी वेबसाइट है और वहां से डिप्लॉयड तक का सफर
7:51:597 hours, 51 minutes, 59 secondsजो था वह कितना आसान था आप देख चुके हो मान लो अगर आपकी वेबसाइट में कुछ चेंजेज करना हो आप क्या क्या चीज करोगे
7:52:087 hours, 52 minutes, 8 secondsचलो मैं वो चीज दिखाता हूं अभी index.htm है ठीक है और यह हमारी वेबसाइट है देखो v7
7:52:167 hours, 52 minutes, 16 secondsलिखा हुआ वर्जन से लेट्स से सो यहां पे देखोगे दिस इज अ डिजाइन v7 ठीक है ये index.htm है मैं इसको एडिट करता हूं
7:52:247 hours, 52 minutes, 24 secondsयूआईसी कर लेते हैं क्योंकि छोटा सा चेंज है v7 की जगह मैं य पे v8 कर देता हूं वेरी सिंपल एक छोटा सा चेंज है और कमिट
7:52:337 hours, 52 minutes, 33 secondsचेंज एंड कमिट चेंजेज ठीक है मैंने यहां पे चेंजेज कमिट किए ओके चेंजेज हो गए v8
7:52:407 hours, 52 minutes, 40 secondsहो गया और मैं आपको एक मैजिक दिखाता हूं आप देखोगे मैंने वहां पे अपने चेंजेज कमिट किए और यहां पे
7:52:487 hours, 52 minutes, 48 secondsडिप्लॉयड वो डिटेक्ट किया और इसने डिप्लॉयड स्टार्ट कर दिया ओके सो यहां पे डिप्लॉयड हो गया है एंड इसके बाद मैं फिर
7:52:557 hours, 52 minutes, 55 secondsसे डोमेन प क्लिक करता हूं एंड यू विल सी यहां पे वर्जन एट हो गया है ना ये देखो ये मैंने पहले वाले भी ओपन करके रखा था ये
7:53:027 hours, 53 minutes, 2 secondsवर्जन सेवन था अब ये वर्जन एट हो गया ओके एंड वर्जन सेवन को भी रिफ्रेश करता हूं सो यहां पे एट आ जाएगा सो दिस इज सो इजी राइट
7:53:117 hours, 53 minutes, 11 secondsआपके पास एक वेबसाइट थी एंड वहां से डिप्लॉयड काफी इजी था अपडेट करना इट्स इजी ऑन द वे तुरंत इमीडिएट यहां पे आपकी
7:53:197 hours, 53 minutes, 19 secondsवेबसाइट भी अपडेट हो जाती है चलो इसके बाद थोड़ा सा और चीजें देख लेते हैं देखो लास्ट कमिट कब हुआ है ठीक है अपडेट तो
7:53:277 hours, 53 minutes, 27 secondsउसकी भी आपको इंफॉर्मेशन यहां पे मिल जाती है ये देखोगे लास्ट कमिट अपडेट index.htm कि क्या चेंजेज हुए हैं वो उसकी
7:53:347 hours, 53 minutes, 34 secondsइंफॉर्मेशन मिल जाएगी एंड इसके बाद कितने कब कब डिप्लॉयड हुए हैं तो ये डिप्लॉयड हिस्ट्री भी आपको यहां पे मिल जाएगी देखो ऑटो बिल्ड है अपडेट इन .
7:53:587 hours, 53 minutes, 58 secondsएंड तो अब देखोगे लेफ्ट साइड में होस्टिंग पे आता है काफी सारे ऑप्शंस हैं एक्सेस कंट्रोल्ड और कस्टम डोमस है कस्टम हेडर एन
7:54:067 hours, 54 minutes, 6 secondsकैसे है एनवायरमेंट वेरिएबल फायरवॉल मॉनिटरिंग प्रीव्यू एंड सो मेनी थिंग्स सीक्रेट्स ऑफ कोर्स एंड कस्टम डोमस पे
7:54:137 hours, 54 minutes, 13 secondsचलते हैं क्योंकि वो हम लोग यूज़ का है अब यहां पे कस्टम डोमस पे आते हैं यू कैन यूज़ ऐड क ऐड डोमेन ठीक है ऐड डोमेन पे
7:54:217 hours, 54 minutes, 21 secondsआते हैं अगर आप एए के राउट 53 सर्विस यूज़ कर रहे हो तो आप यहां डायरेक्टली अब देखो यहां पे कुछ दिखाई नहीं दे रहा अ तो आप
7:54:297 hours, 54 minutes, 29 secondsक्या कर सकते हो अगर राउट 53 में अगर आपका डोमेन है तो यू कैन यूज़ इट चलो मैं आपको जल्दी से करके ही दिखा देता हूं राउट 53 पे चलते हैं एक नया टैब खोल दिया इन्होंने
7:54:387 hours, 54 minutes, 38 secondsठीक है राउट 53 मैं एक डेडिकेटेड वीडियो है अगर आप अच्छे से समझना चाहो उसकी पूरी वर्किंग तो प्लीज वो देख लेना राउट 53 में
7:54:477 hours, 54 minutes, 47 secondsआप अपने डीएनए सर्वर को मैनेज कर सकते हो और यहां पे डोमेन रजिस्टर मतलब खरीद भी सकते हो अ फीस लगती है उसमें ठीक है तो
7:54:557 hours, 54 minutes, 55 secondsअगर मैं पहले तो आपको दिखा दूं डोमेन रजिस्टर्ड मतलब रजिस्ट्रेशन में मैंने एक डोमेन नेम परचेस करके रखी है माय वेब
7:55:047 hours, 55 minutes, 4 secondsसाइडेड क्क यह सबसे सस्ती होती है $3 में मिल गई थी मेरे को परा एनम तो मैंने लेके रखा है जस्ट फॉर द डेमो पर्पस आप लोगों को
7:55:117 hours, 55 minutes, 11 secondsदिखाने के लिए सो ये जो मेरा रजिस्टर डोमेन है मैं इसको य जल्दी से इसको कॉपी कर लेता हूं ठीक है इसकी क्योंकि जरूरत पड़ेगी इसके सिंपली मुझे क्या है होस्टेड
7:55:197 hours, 55 minutes, 19 secondsजोन बनाना है होस्टेड जोन मतलब डीएनएस सेटिंग क्रिएट होस्टेड जन ओके सो माय वेबसाइट डेमो ड क्लिक तो मतलब आपकी केस
7:55:277 hours, 55 minutes, 27 secondsमें एकवा या तो मतलब यू नो example.com तो इस टाइप का आपका रहेगा कॉ जैसा भी है पब्लिक होस्टेड जन रखना है एंड क्रिएट होस्टेड जन
7:55:367 hours, 55 minutes, 36 secondsइट्स वेरी सिंपल ठीक है सो आपको डोमेन नेम जो भी आपका खरीदा हुआ डोमेन है आपको सिफ वही यूज करना है ठीक है अगर आप देखोगे
7:55:447 hours, 55 minutes, 44 secondsजैसे ही मैं बनाता हूं यहां पे नेड सर्वर सर्वर बन जाता है नेक्स्ट स्टेप ये है कि ये जो अगर आप वैल्यू देख रहे हो ठीक है ये
7:55:527 hours, 55 minutes, 52 secondsजो डीएनएस की वैल्यूज सो जो मैंने डोमेन परचेस किया है ठीक है मैं इसको ओपन करता हूं और ये प्रोसेस आप कहीं भी कर सकते हो
7:56:007 hours, 56 minutesठीक है अगर आपने जो डोमेन है वो बाहर से परचेस किया हो तो आपको सिंपल क्या है उसकी सेटिंग्स में जाना और नेम सर्वर जो है
7:56:077 hours, 56 minutes, 7 secondsवहां पे आपको चेंजेज करने पड़ेंगे एक्शन में जाके एडिट नेम सर्वर ठीक है तो ये चार हो गए ओके तो इसमें क्या है वन बा वन हम
7:56:147 hours, 56 minutes, 14 secondsलोग कॉपी कर देंगे कॉपी एंड यहां पे रिप्लेस रिप्लेस ठीक है इस तरह से चारों
7:56:217 hours, 56 minutes, 21 secondsको मैं अपडेट कर देता हूं तो ये नेम सर्वर हो गए सेव चेंजेज सो क्या किया है मैं थोड़ा सा फ्लो बता देता हूं जो नेम सर्वर
7:56:307 hours, 56 minutes, 30 secondsअभी हम लोग ने अपडेट किए हैं इसका मतलब क्या है जैसे ही ब्राउजर पे कोई यह सर्च करेगा एडब्ल्यू एस के नेम सर्वर या तो
7:56:397 hours, 56 minutes, 39 secondsहोस्टेड जोन तक उसकी रिक्वेस्ट आ जाएगी होस्टेड जोन से कहां जाएगी रिक्वेस्ट वो चीजें अभी हमारी बाकी है तो यह काम हो गया
7:56:477 hours, 56 minutes, 47 secondsहमारा चलिए अभी हम लोग जाते हैं फिर से कस्टम डोमेन पे ठीक है और मैं इसको रिफ्रेश करता हूं आप देखोगे तो यहां पे आ
7:56:557 hours, 56 minutes, 55 secondsगया माय वेबसाइट डेमो ड क्लिक सिंपल एंड उसके बाद कॉन्फ़िगर डोमेन एंड इसमें देखोगे कि
7:57:287 hours, 57 minutes, 28 secondsथोड़ा सा कुछ मिनट्स लगते हैं अभी हम लोग वेट करते हैं एंड आई विल शो यू सो यहां पे हमारा कस्टम डोमेन का सेटअप भी कंप्लीट हो गया है आप देख सकते हो अवेलेबल है ओके माय
7:57:377 hours, 57 minutes, 37 secondsवेबसाइट डेड क्क यह हो गई अपनी वेबसाइट का नाम अ एक्सेस करके देखते हैं सो ब्राउजर पे अभी हम लोग अपनी वेबसाइट को ओपन करते
7:57:447 hours, 57 minutes, 44 secondsहैं
7:57:587 hours, 57 minutes, 58 secondsवेरी इजी एंड जो अभी हम लोग ने राउट 53 में जो हम लोग ने जो चीजें करी थी कहां पे डोमेन
7:58:067 hours, 58 minutes, 6 secondsके साथ वो अगर लेट्स से अगर आप होस्टिंगर से आपने एक डोमेन खरीदा है ये देखोगे माय एल वेबसाइड एकवा आप सिंपल इसको मैनेज
7:58:147 hours, 58 minutes, 14 secondsकरोगे एंड लेफ्ट साइड में आप जाओगे डीएनएस नेम सर्वर यहां पे चेंज नेम सर्वर पे करना ठीक है ये देखोगे चार नेम सर्वर है वही
7:58:217 hours, 58 minutes, 21 secondsस्टेप सेम रहेगा जो चार नेम सर्वर होस्टेड जन हम लोग ने बनाया था ठीक है चेंज नेम सर्वर करके इसमें ठीक है मतलब रिप कर देना
7:58:287 hours, 58 minutes, 28 secondsऐड कर देना सेव कर देना सो आप होस्टिंगर से भी जो डोमेन खरीदते हो आप उसको यूज कर पाओगे काफी आसानी से सो अभी तक हम लोग ने
7:58:357 hours, 58 minutes, 35 secondsदेखा कि एक आपके पास ऑलरेडी वेबसाइट है आपको डिप्लॉयड करना है कस्टम डोमेन ऐड करना है
7:58:427 hours, 58 minutes, 42 secondsसर्टिफिकेट्स ऐड करने हैं एंड इसके साथ कंटीन्यूअस डिप्लॉयड भी करना है कि आपने वहां पर चेंजेज किए एंड इमीडिएट आपकी जो
7:58:497 hours, 58 minutes, 49 secondsवेबसाइट है अपडेट हो जाए अपग्रेड हो जाए सो दिस इज अ वेरी यूजफुल ओके सो एक बेसिक फ्लो प्रैक्टिकल
7:58:597 hours, 58 minutes, 59 secondsकरके देख लिया हम लोग ने एंड समझ गए होंगे कि एमप्लीफाई क्या क्या यूज है बट थोड़ा सा और डिटेल में बात करते हैं स्टार्टिंग में हम लोग ने बोला था प्लेटफॉर्म है यहां
7:59:087 hours, 59 minutes, 8 secondsपे बिल्डिंग डिप्लॉयड एंड होस्टिंग फुल स्टैक वेब एप्लीकेशंस में हेल्प होती है डिप्लॉयड एंड होस्टिंग तो देख ली बट ये बिल्डिंग क्या होता है क्या यह हमें
7:59:167 hours, 59 minutes, 16 secondsवेबसाइट बनाने में हेल्प भी करता है और करता है तो कैसे और किन चीजों में तो यस यह करता है वो चीज भी हम लोग कवर करेंगे
7:59:247 hours, 59 minutes, 24 secondsएंड इसके साथ फुल स्टैक वेब फुल स्टैक मतलब थोड़ा सा ब्रीफ आपको कर दे देता हूं जहां पे आपका फ्रंट एंड बैक एंड आपके डेटाबेस
7:59:327 hours, 59 minutes, 32 secondsऑथेंटिकेशन एपीआई एंड स्टोरेज इतनी सारी चीजें सब मिक्स अप रहती हैं लाइक एक टोटल कंबाइंड एक फुल फ्लैश एप्लीकेशन होती है
7:59:417 hours, 59 minutes, 41 secondsअब इसमें हर एक चीजों में एक अलग टेक टेक टेक्नोलॉजी ऐड होती है जैसे कि फ्रंट एंड में रिएक्ट एंगलर और बैक एंड में आपका
7:59:497 hours, 59 minutes, 49 secondsजावा पीएचपी नट ओके एंड डेटाबेस सो मेनी थिंग्स ये जो हमारा एमप्लीफायर सर्विस है
7:59:567 hours, 59 minutes, 56 secondsये इतनी सारी टेक जो है मतलब जो मार्केट में ट्रेंडिंग टेप्स टक्स हैं ये सपोर्टेड करता है और वहां पे हम लोग ने गेम गेमिंग
8:00:048 hours, 4 secondsभी बोला था इसलिए ए ओ सॉरी गेमिंग या तो मोबाइल एप्स सॉरी सो ए कटलिन ठीक है सो ये सारी चीजें भी हैं जावा जावास्क्रिप्ट
8:00:138 hours, 13 secondsएंगल रिएक्ट सो यहां पे आपको सब कुछ मिल जाएगा सो अगर आप सोच रहे हो कि कुछ लेटेस्ट टेक के साथ कुछ बिल्ड करना तो
8:00:228 hours, 22 secondsयहां पे आपको ठीक है कोई दिक्कत नहीं होगी इसके अलावा एमप्लीफाई के की पॉइंट्स क्या है कि जैसे कि अगर बिल्डिंग में अी अगर आपको हेल्प करता है तो कहां करता है देखो
8:00:308 hours, 30 secondsजब आप एक मान लो एक वेब एप्लीकेशन बना रहे हो ठीक है जो इसका इंटरनल लॉजिक है जो इसके फीचर्स है जो यूनिक है मतलब अगर आपका एक एप्लीकेशन का यूएसपी है वो तो आप खुद
8:00:398 hours, 39 secondsही बिल्ड करोगे खुद ही उसका लॉजिक बिल्ड करोगे खुद बनाओगे लेकिन कुछ जनरल चीजें होंगी राइट जनरल चीजें मतलब जैसे कि मान
8:00:468 hours, 46 secondsलो अकाउंट क्रिएशन लॉग इन लॉगआउट ओके अब ये तो सारी वेबसाइट में ऑलमोस्ट सेम ही होता है भाई लॉग इन लॉग आउट अकाउंट
8:00:548 hours, 54 secondsक्रिएशन राइट सो अब जैसे कि यूजर लॉगइन हो गया फिर ऐसी चीजें है ना फिर डाटा स्टोरेज भाई स्टोरेज अ मतलब स्टोरेज एक तो सर्विस
8:01:038 hours, 1 minute, 3 secondsप्रोवाइड करना क्योंकि यहां पे s3 सर्विस है इनके पास इसके बाद डेटाबेस अगर यूज़ कर रहे हो तो डायनेमो डीवी वगैरह है इनके पास ओके इसके बाद एपीआई है इनके पास एंड सो
8:01:118 hours, 1 minute, 11 secondsमेनी थिंग्स सो ये सारी चीजें कंबाइन आप कर सकते हो एंड एडब्ल्यू है तो ऑफकोर्स आपको इसका यूज़ भी पता है कि जैसे य मान
8:01:188 hours, 1 minute, 18 secondsलो डेटाबेस है डेटाबेस है मान लो तो वो स्केल कर सकता है आपकी वेबसाइट के यूज केस के अकॉर्डिंग कल को भाई आपके वेबसाइट पे
8:01:278 hours, 1 minute, 27 secondsबहुत ज्यादा अगर ट्रैफिक आ रहा है यूजर्स बढ़ रहे हैं तो व भी हैंडल हो जाएगा ठीक है तो यह तो है चीजें बट अभी यह स ये सारी
8:01:358 hours, 1 minute, 35 secondsचीजें करके देखेंगे ना तो आपको ज्यादा समझ में आएगा तो एक काम करते हैं रिएक्ट बेस्ड टू
8:01:438 hours, 1 minute, 43 secondsडू एप्लीकेशन हम लोग डिप्लॉयड लेट्स सी हाउ इट वर्क्स ठीक है कैसे करना है बताता
8:01:498 hours, 1 minute, 49 secondsहूं एमप्लीफाई ये अभी एडब्ल्यू के कंसोल पे गए अभी हम लोग ने एक बनाया था एप्लीकेशन एंड इसी में आप देखोगे ये राइट
8:01:578 hours, 1 minute, 57 secondsहैंड साइड में टॉप पे डॉक्स का एक लिंक है ओके सो यहां पे आ जाओ लेफ्ट साइड में आप देखोगे क्विक स्टार्ट एंड इसके बाद आई विल
8:02:068 hours, 2 minutes, 6 secondsसे कि आप वीडियो को छोड़ के चाहो तो ठीक है आप यहां पे फॉलो भी कर सकते हो इट्स वेरी इजी बट हां मैं वीडियो के साथ-साथ
8:02:138 hours, 2 minutes, 13 secondsकुछ-कुछ चीजें बताता भी जाऊंगा इसमें एंड क्विक स्टार्ट करने से पहले आपको क्या-क्या चाहिए ऑफकोर्स एड कंसोल जिस पे
8:02:208 hours, 2 minutes, 20 secondsआप लॉक्ड इन हो एंड इसके साथ-साथ आपको गेट हब चाहिए वेरी सिंपल ठीक है अभी आपके पास गेट हब में रिट का तो कोई एप्लीकेशन है ही
8:02:278 hours, 2 minutes, 27 secondsही नहीं आप क्या करेंगे ठीक है उसके लिए भी मैं बताता हूं यह जो एडब्ल्यू है इसमें आपको एक एप्लीकेशन सैंपल एप्लीकेशन मिलती
8:02:358 hours, 2 minutes, 35 secondsहै देखो क्रिएट रेपो फ्रॉम टेंप्लेट सो मैं जैसे ही उस परे क्लिक करता हूं सो इसके बाद यह एक ऑप्शन देता है कि अपने गट
8:02:448 hours, 2 minutes, 44 secondsहब अकाउंट में एक रेपो बनाओ एंड कौन सा टेंप्लेट यूज करो एडब्ल्यू सैंपल का एमप्लीफाई रिएक्ट टेंप्लेट ओके एंड नाम भी
8:02:528 hours, 2 minutes, 52 secondsइसने खुद से सजेस्ट कर दिया अवेलेबल है एंड पब्लिक यूज़ करना है एंड क्रिएट रेपो ओके सो हेयर य गो अभी रिएक्ट बेस्ड एक
8:03:008 hours, 3 minutesसैंपल एप्लीकेशन आपके गट हब अकाउंट में आ गया ओके इसके बाद क्या करना है वो तो आपको पता है चलिए डायरेक्टली हम लोग चलते हैं
8:03:088 hours, 3 minutes, 8 secondsअपने ऐप पे ठीक है अ सो हम लोग ऑल एप्स पे चलते हैं क्रिएट न्यू पप ठीक है क्योंकि एक नया ऐप है गेट हब से ही करना है
8:03:168 hours, 3 minutes, 16 secondsनेक्स्ट करते हैं इसके बाद हम लोग अपडेट गेट गेट हब परमिशन करते हैं ठीक है तो आपको इस टाइप का पेज दिखेगा ये हमारा ही
8:03:238 hours, 3 minutes, 23 secondsहै ऑल रेपो और सिलेक्टेड रेपो सिलेक्टेड रेपो में आप क्या है जो नई वाली जो वेबसाइट है या तो रेपो जो अभी हम लोग ने
8:03:308 hours, 3 minutes, 30 secondsबनाई है उसको मैं क्लिक करता हूं ठीक है एंड सेव कर देता हूं ट्स इट ठीक है सेव हो गई ओके इसके बाद हम लोग फिर से अपने
8:03:388 hours, 3 minutes, 38 secondsअकाउंट पे जाते हैं सो इसके बाद आप सिंपली यहां पे रिफ्रेश कर सकते हो एंड सिलेक्ट पे करते हैं सो यह देखोगे यह रिएक्ट वाली
8:03:468 hours, 3 minutes, 46 secondsअभी एमप्लीफाई रिएक्ट वाली एप्लीकेशन हमारे पास आ गई मैं उसको सेलेक्ट करता हूं नेक्स्ट करता हूं अभी एक कुछ चीजें मतलब
8:03:538 hours, 3 minutes, 53 secondsआप इंटरेस्टिंग देखोगे कि यह रिएक्ट बेस्ड एप्लीकेशन थी और जब हम हम लोग ने इसको सिलेक्ट किया सो आप देखोगे
8:04:018 hours, 4 minutes, 1 secondएमप्लीफाई जनरेशन म टू इसने डिटेक्ट कर लिया इस फ्रंट एंड को बिल्ड करने की कोई
8:04:098 hours, 4 minutes, 9 secondsकमांड है क्या ऑफकोर्स एनपीएम रन बिल्ड तो यह आप कमांड इस तरह से दे सकते हो लद यह रिएक्ट एप्लीकेशन थी तो इसने ऑटोमेटिक
8:04:178 hours, 4 minutes, 17 secondsडिटेक्ट करके यहां पे लिख दिया ओके सो ये चीजें खुद कर देता है वो ओके एंड इसके बाद नेक्स्ट करते हैं रिव्यू आप कर सकते हो ऑल
8:04:258 hours, 4 minutes, 25 secondsदो ऑल सेट एंड सेव एंड डेप्लॉय मतलब वो सेम प्रोसेस था मैं छोटी-छोटी कुछ डिटेल थी वह आपको बताया ओके सो यह एप्लीकेशन भी
8:04:338 hours, 4 minutes, 33 secondsअपनी डिप्लॉयड हो गई है एंड मैं इस पे सिंपली क्लिक करता हूं सो कुछ इस टाइप की एप्लीकेशन मिलती है टू डू लिस्ट है आप सिंपली यहां पे कुछ-कुछ चीजें ऐड कर सकते
8:04:408 hours, 4 minutes, 40 secondsहो जैसे कि हाय मतलब मैं कुछ भी ऐड कर रहा हूं ठीक है ठीक है सो सीएसएस एंड ऑल ठीक
8:04:478 hours, 4 minutes, 47 secondsहै तो आप देखोगे ये चीजें यहां पे मतलब जो मेरी टू डू लिस्ट है भाई दिन की वो ऐड होती जा रही है क्या-क्या मुझे करना है बट इसमें सेर्फ फंक्शनैलिटी है कि न्यू मतलब
8:04:568 hours, 4 minutes, 56 secondsकुछ भी चीजें ऐड करना दैट्ची है अभी चलो एप्लीकेशन और दिखाता
8:05:058 hours, 5 minutes, 5 secondsहूं एंड इसके बारे में थोड़ा सा और बात करते हैं यह एप्लीकेशन अभी हम लोग ने डिप्लॉयडी
8:05:128 hours, 5 minutes, 12 secondsमैं इसमें सिंपली मेन में जाता हूं एंड इसके बारे में कुछ बात करते हैं अभी सिंपली मान लो अगर डेटा पे जाता है
8:05:198 hours, 5 minutes, 19 secondsएंड डेटा मैनेज है सो मैं डेटा मैनेजर पे जाता हूं सो यू विल सी कि जो भी अभी हम लोग ने अपनी जो वेबसाइट है इसमें मतलब जो
8:05:278 hours, 5 minutes, 27 secondsभी लिस्ट हम लोग ने ऐड करी थी ठीक है सो यू विल सी कि यहां पे डाटा के अंदर डेटा मैनेजर है एंड यहां पे आप ये सारी चीजें
8:05:368 hours, 5 minutes, 36 secondsदेख सकते हो अब ये सारी चीजें भाई स्टोर कहां पे हो रही हैं यू विल यू विल सी कि मान लो मैं ठीक है मैं इसको पहले एक दूसरा
8:05:458 hours, 5 minutes, 45 secondsटैब ओपन कर लेता हूं सो मैं सिंपली क्या करता हूं सर्विसेस में जाता हूं डायनामो डीबी पे चलते हैं कि मैं जैसे ही डायनेमो
8:05:528 hours, 5 minutes, 52 secondsडीवी ओपन करता हूं सो यहां पे टेबल्स में दो टेबल्स दिखाई दे रही हैं एक टू डू जो कि अभी हम लोग ने जो ऐप बनाया है उसका एंड
8:06:018 hours, 6 minutes, 1 secondएक यूजर्स ठीक है इसका मतलब क्या है कि वो एप्लीकेशन में जो भी डेटा स्टोर हो रहा है वो डेटाबेस में हो रहा है राइट सो ये
8:06:098 hours, 6 minutes, 9 secondsडेटाबेस इसने ऑलरेडी बना के दे दिया इसके बाद हम लोग चलते हैं डिप्लॉयड पे ठीक है तो डिप्लॉयड में अ नीचे चलते हैं एंड
8:06:178 hours, 6 minutes, 17 secondsडिप्लॉयड बैक एंड रिसोर्सेस मतलब बैक एंड में क्या चीजें चल रही हैं क्या-क्या रिसोर्सेस बन रहे हैं उसका आईडिया अगर
8:06:258 hours, 6 minutes, 25 secondsआपको चाहिए तो बैक एंड रिसोर्सेस पे आ जाओ एंड इसमें आपको काफी सारी चीजें मिल जाएंगी ठीक है जैसे कि एड कॉग्निटो ओके वो
8:06:348 hours, 6 minutes, 34 secondsयूज़ हो रहा है आईएम वगर पॉलिसीज एंड ऑल ये सब चीजों में तो चेंज होगा लैडा फंक्शन ठीक है तो यहां पे लैडा फंक्शन भी यूज़ हो
8:06:418 hours, 6 minutes, 41 secondsरहा है ओके तो आप इसको क्लिक करके देख सकते हो कि लैडा फंक्शन बना होगा ओके अ जैसे कि मान लो चलो यहां पे चलते हैं
8:06:498 hours, 6 minutes, 49 secondsएंड लैडा में अगर क्लिक करता हूं सो यहां पे यू कैन सी कि देखो एंपलीफायर एंपलीफायर जो अभी हम लोग एप्लीकेशन यूज कर रहे हैं
8:06:578 hours, 6 minutes, 57 secondsठीक है तो उसके लैडा फंक्शंस भी बने हैं अभी ये तो हमारा मेन एप्लीकेशन हो गया राइट एंड मान लो इसमें अगर हमको कोई
8:07:068 hours, 7 minutes, 6 secondsचेंजेज करना हो या तो टेस्टिंग करना हो तो वो कैसे करेंगे इसके लिए हम लोग चलते हैं अपने गेट हब पे ओके ये गेट हप पे हमारा
8:07:148 hours, 7 minutes, 14 secondsकोड है जो कोड हम लोग ने वहां से कॉपी किया था सैंपल कोड से इसके बाद ओके वन मोर
8:07:208 hours, 7 minutes, 20 secondsथिंग कि यहां से आगे बढ़ने के लिए आपके पास नट जेएस इंस्टॉल्ड होना चाहिए
8:07:288 hours, 7 minutes, 28 secondsगिट हब इंस्टॉल्ड होना चाहिए सेट अप होना चाहिए ओके सो यह चीजें जरूरी हैं लोकली काम करने के लिए कोड पे गए एचटीटीपीएस है
8:07:378 hours, 7 minutes, 37 secondsयह अपन ने ठीक है इसको मैंने सेलेक्ट कर लिया इसके बाद मैं सिंपली वीएस कोड ओपन करूंगा वीएस कोड अ मैं यूज़ कर रहा हूं
8:07:458 hours, 7 minutes, 45 secondsबाकी आप कुछ भी यूज कर सकते हो ऑल द वीएस कोड इज द बेस्ट ओके ऐसे ओपन फोल्डर कर सकते हो एक खाली फोल्डर कोई भी देखो यहां
8:07:528 hours, 7 minutes, 52 secondsपे ब्लैंक फोल्डर है अभी यहां पे मैं आपको चीजें दिखा देता हूं जैसे कि नट h v करके दिखा देता हूं नोड मेरे पास ऑलरेडी
8:07:598 hours, 7 minutes, 59 secondsइंस्टॉल्ड है ओके एनपीएम नट पैकेज मैनेजर वो भी इंस्टॉल्ड है ठीक है इसके बाद गेट
8:08:078 hours, 8 minutes, 7 secondsतो होना ही चाहिए ऑफकोर्स क्योंकि गेट के साथ हम काम कर रहे हैं ठीक है तो गेट क्लोन हम लोग कर लेते हैं ओके एंड जो अभी
8:08:148 hours, 8 minutes, 14 secondsहम लोग ने यूआरएल कॉपी किया था एचटीटीपी वाला ठीक है सो जैसे ही हम करते हैं सो यू
8:08:208 hours, 8 minutes, 20 secondsविल सी कि एमप्लीफाई वाला जो हमारा एप्लीकेशन है वह पूरा यहां पर आ गया ठीक
8:08:278 hours, 8 minutes, 27 secondsहै तो ग
8:08:578 hours, 8 minutes, 57 secondsकरने के लिए जो भी लाइब्रेरीज एंड ऑल यूज़ होगी तो वो इंस्टॉल कर देगा एनपीएम इंस्टॉल आप करो ओके सो इसमें नोड मॉड्यूस आ जाएगा बेसिकली तो आप देखोगे यहां पे नोड
8:09:058 hours, 9 minutes, 5 secondsमॉड्यूस एक फोल्डर ऐड हो गया है सो दैट उसकी मदद से आप इस एप्लीकेशन को रन कर सकते हो ओके सो एनपीएम इंस्टॉल कर लिया
8:09:148 hours, 9 minutes, 14 secondsठीक है क्या हम इसके बाद अपने एप्लीकेशन को रन कर सकते हैं अब देखो थोड़ा सा विजुलाइज करते हो कभी भी अगर कहीं भी ऐसे
8:09:228 hours, 9 minutes, 22 secondsट्रबल शूटिंग कर रहे हो तो फंस जाओ ना तो हमेशा फ्लो समझने की कोशिश किया करो देखो जब हमारा एप्लीकेशन आपके एडब्ल्यू एस
8:09:298 hours, 9 minutes, 29 secondsकंसोल पर था या तो गेट हब से जो यूज कर रहे थे वह एप्लीकेशन में क्या-क्या चीजें यूज हो रही थी आप सोचो आपका एडब्ल्यू एस
8:09:388 hours, 9 minutes, 38 secondsका डायनामो डीबी लडा फंक्शन ठीक है तो बैक एंड एडब्ल्यू एस में बैक एंड रिसोर्सेस यूज हो रहे थे ठीक है अभी यह जो कोड है
8:09:468 hours, 9 minutes, 46 secondsआपकी वेबसाइट का यह कहां पर है आपके लोकल एनवायरमेंट में प्रेजेंट है क्या आप अपने लोकल एनवायरमेंट से
8:09:558 hours, 9 minutes, 55 secondsएडब्ल्यू एस के डेटा बेस और ये रिसोर्सेस से कनेक्ट कर पाओगे नहीं राइट तो उसके लिए क्या करना पड़ेगा चलो मैं आपको बता देता
8:10:028 hours, 10 minutes, 2 secondsहूं सिंपल सा है ज्यादा घुमाऊंगा नहीं बात ठीक है तो ये ऐप में गए अपन इसके बाद यह मेन पे गए ठीक है डिप्लॉयड वाले में एंड
8:10:118 hours, 10 minutes, 11 secondsडिप्लॉयड बैक एंड रिसोर्सेस पे आए ठीक है इसके बाद एक फाइल देखोगे डाउनलोड एलीफा मतलब अ output.txt
8:10:278 hours, 10 minutes, 27 secondsलोकेशन पे रख देना है मतलब यह जो मेन फोल्डर है ना मेन फोल्डर इसमें ही आपको ये फाइल को रख देना है तो मैं जल्दी से रख
8:10:348 hours, 10 minutes, 34 secondsदेता हूं फिर दिखाता हूं आपको ओके सो यह चीज का आपको ध्यान रखना है
8:10:428 hours, 10 minutes, 42 secondsसिंपली कि जो एंपलीफायर आउटपुट जन वाली जो फाइल है वो यह प्रोजेक्ट फोल्डर के अंदर होनी चाहिए देखो ये मैंने इसको ठीक है एक्सपेंड किया तो यहां पे यह फाइल दिख रही
8:10:508 hours, 10 minutes, 50 secondsहै ओके वेरी सिंपल इसके बाद सिंपली इसको रन करने बहुत ही आसान है एपीएम रन डेव एनपीएम रन डेव मैं इसको करता
8:11:008 hours, 11 minutesहूं सो यहां पर आपको देखोगे इस इस टाइप की लिंक दिखेगी आप सिंपली यहां पर ब्राउजर प जाकर इसको एक्सेस करोगे इसको एक्सेस करता
8:11:078 hours, 11 minutes, 7 secondsहूं सो यू विल सी कि हमारा जो एप्लीकेशन है वो आ गया है वन मोर थिंग इंटरेस्टिंग है कि यहां पे मतलब मैं लोकली इसको रन कर रहा हूं सो ये तीनों चीज यहां पर मिल रही
8:11:158 hours, 11 minutes, 15 secondsहै दिखाई दे रही है दैट मींस और क्यों आ रही है क्योंकि यह डेटाबेस में स्टोर्ड है ओके अब मैं आपको एक इंटरेस्टिंग चीज बताता हूं न्यू किया मैंने मान लो मैं इसमें ऐड
8:11:258 hours, 11 minutes, 25 secondsकरता हूं न्यू फ्रॉम लोकल ठीक है न्यू फ्रॉम लोकल मतलब यह लोकल एनवायरमेंट से मैंने कर रहा हूं क्योंकि आप लिंक देख
8:11:338 hours, 11 minutes, 33 secondsसकते हो लोकल होस्ट कॉलन 5173 लेकिन वेरी इंटरेस्टिंग चीज कि आप लोकल से मैंने यह नया ऐड किया उसके बाद
8:11:418 hours, 11 minutes, 41 secondsमैं डायरेक्टली चलता हूं अपने वेबसाइट पे यह हमारा मेन एप्लीकेशन है डटा पे हम लोग
8:11:478 hours, 11 minutes, 47 secondsडेटा मैनेजर प चलते हैं सो डेटा मैनेज मैनेजर प आप देख सकते हो न्यू फ्रॉम लोकल ठीक है सो यहां से आ गया दैट मींस कि अभी
8:11:558 hours, 11 minutes, 55 secondsआपका जो लोकल एनवा मेंट है इवन आप टेस्टिंग कर रहे हो वह आपका कनेक्टेड कहां से है एडब्ल्यू रिसोर्स से यह जो क्विक
8:12:038 hours, 12 minutes, 3 secondsस्टार्ट गाइड है ना इसमें सारे स्टेप दिए हुए हैं लद मैं मैंने वही फॉलो किया ठीक है डेटा मैनेजर इसमें क्या-क्या मिलेगा सेट अप लोकल एनवायरमेंट क्लोन कैसे करना
8:12:128 hours, 12 minutes, 12 secondsहै इसके बाद यह एमप्लीफाई आउटपुट वाली जो फाइल है उसको डाउनलोड करना एंड सो मेनी थिंग ठीक
8:12:208 hours, 12 minutes, 20 secondsहै कि हम लोग ने टू डू लिस्ट बना तो ली लेकिन इसमें इसमें डिलीट करने का कोई भी ऑप्शन नहीं है तो वो चीज हम लोग ऐड करते
8:12:278 hours, 12 minutes, 27 secondsहैं ठीक है सो इसमें बता भी रहा है आप क्विक स्टार्ट गाइड को ठीक है फॉलो कर सकते हो डिलीट फंक्शनैलिटी ऐड करना कैसे करेंगे सबसे पहले आपको फाइल ढूंढनी है .
8:12:578 hours, 12 minutes, 57 secondsकोड ठीक है फंक्शन एक ऐड कर रहे हैं डिलीट टू डू इसको कॉपी किया ठीक है और इसमें हम लोग चलते हैं एंड यू विल सी कि देखो
8:13:068 hours, 13 minutes, 6 secondsक्रिएट टू डू है एक फंक्शन ऑलरेडी है एंड डिलीट अभी हम लोग एक डिलीट फंक्शन यूज कर रहे हैं डिलीट टू डू एक फंक्शन हम लोग ने
8:13:148 hours, 13 minutes, 14 secondsऐड किया इसके बाद और क्या चीज ऐड करनी है एक कॉपी करना है ऑन क्लिक ठीक है ये डिलीट
8:13:218 hours, 13 minutes, 21 secondsटू डू एंड यह करना है की के ऊपर ओके तो हम लोग इसमें चलते हैं फिर से एंड यू विल सी
8:13:288 hours, 13 minutes, 28 secondsकि यहां पे देखो यहां की दिया हुआ है ठीक है एंड इसके ऊपर मैं इसको पेस्ट कर देता हूं ठीक है सिंपली इसको मैंने सेव किया
8:13:368 hours, 13 minutes, 36 secondsअभी हम लोग इसको रिफ्रेश करते हैं ठीक है अब लेट्स सी कि ये डिलीट फंक्शन काम कर रहा है कि नहीं सी अभी मैं जैसे कि ये
8:13:448 hours, 13 minutes, 44 secondsमेरा काम हो गया मैं क्लिक कर रहा हूं यह देखो डिलीट फंक्शन हमारा परफेक्टली काम कर रहा है दिस इज अमेजिंग राइट मतलब आपने
8:13:528 hours, 13 minutes, 52 secondsदेखा डिलीट फंक्शन ऐड करना कितना आसान है चलो अब इसके बाद और आगे करते हैं थोड़ा सा लॉग
8:13:598 hours, 13 minutes, 59 secondsइन ठीक है लेट्स से कि यह जो टू डू एप्लीकेशन है कोई गेम है या तो कुछ भी है अगर आपको चाहते हो कि कोई भी यूजर पहली
8:14:078 hours, 14 minutes, 7 secondsबार उसको यूज करें तो उस परे अकाउंट बनाए पहले क्रिएट करें अकाउंट एंड उसके बाद लॉग इन करें तो ही वह उसको यूज कर पाए वेरी
8:14:168 hours, 14 minutes, 16 secondsसिंपल मतलब लॉग यूजर क्रिएशन एंड लॉगइन वाली फैसिलिटी अपन को ऐड करनी है अगर आप देखोगे कि अगर आप खुद से करो तो आपको इसका
8:14:258 hours, 14 minutes, 25 secondsयूआई बनाना पड़ेगा पेज बनाना पड़ेगा बैक एंड रखना पड़ेगा ऑथेंटिकेशन कि यूजर का ईमेल ईमेल वेरिफिकेशन कितनी सारी चीजें
8:14:348 hours, 14 minutes, 34 secondsहैं अभी आप देखना कि एमप्लीफायर से अपन कितने इजली इसको कर पाएंगे है ना इसमें भी इसमें बताया हुआ है देखो सोर्स में मेन
8:14:428 hours, 14 minutes, 42 secondsडटी एक है चलो अपन वाली फाइल पहले निकाल लेते हैं सोर्स में मेन टीए एक हमारे पास फाइल
8:14:508 hours, 14 minutes, 50 secondsहै वेरी सिंपल ठीक है अब इसमें हमको क्या करना है इंपोर्ट करना है ऑथेंटिकेटर ठीक है तो मैं कॉपी करता हूं और सबसे ऊपर हम
8:14:598 hours, 14 minutes, 59 secondsलोग यहां पर कहीं तो ऐड कर देते हैं ठीक है ये देखो ऑथेंटिकेटर ऐड कर दिया ठीक है इसके बाद एक और कॉपी करते हैं स्टाइल्ड
8:15:068 hours, 15 minutes, 6 secondsसीएए क्योंकि स्टाइल्स इसलिए कर रहे हैं क्योंकि इसमें हमारा वो एक यूआई का मतलब
8:15:138 hours, 15 minutes, 13 secondsयू नो यूजर नेम पासवर्ड का एक मतलब फ्रंट एंड जैसा आएगा इसके बाद हम लोग यह नीचे वाला जो कोड है कॉपी करेंगे ठीक है और
8:15:228 hours, 15 minutes, 22 secondsइसमें क्या है जो रिएक्ट वाला पूरा ब्लॉक हटा के हम लोग इसको कॉपी कर देंगे तो ऑथेंटिकेटर हम लोग ने और ऐड कर दिया है एक
8:15:318 hours, 15 minutes, 31 secondsऐप को अंदर यूज करके वेरी सिंपल ठीक है अभी इसके बाद क्या करना है .
8:15:578 hours, 15 minutes, 57 secondsएक पप नाम का यह जो फंक्शन है इसके अंदर हमको क्या यूज करना है कांस्टेंट साइन आउट ठीक है तो अगर आप देखोगे ये प वाला फंक्शन
8:16:048 hours, 16 minutes, 4 secondsहै इसके अंदर यहां पर हम लोग कंस्ट यूज कर सकते हैं इसके बाद रिटर्न मेन तो मतलब मेन
8:16:138 hours, 16 minutes, 13 secondsके ऊपर एक अपन को बटन यूज करनी है तो मैं कॉपी करता हूं तो हम नीचे देखते हैं मेन कहां ऑप्शन मिलता है तो आपको यह प्लेसमेंट
8:16:218 hours, 16 minutes, 21 secondsकी थोड़ी सी यू नो ध्यान रखना पड़ेगा यह देखो मेन दिखा अब मेन के जस्ट ऊपर मैं यहां पे बट यहां पे ओपन करते मतलब एक साइन
8:16:288 hours, 16 minutes, 28 secondsआउट क्योंकि देखो लॉग इन का बटन होगा तो साइन आउट भी करना पड़ेगा तो साइन आउट हम लोग एक बटन ऐड कर रहे हैं वेरी सिंपल अब
8:16:368 hours, 16 minutes, 36 secondsहमारा हो गया खत्म सिंपल अगर आप देखोगे छोटा सा चेंज था एक दो चीजें चेंज करी थी एंड य कैन यू इमेजिन लाइक सी मैंने मुझे
8:16:438 hours, 16 minutes, 43 secondsरिफ्रेश भी नहीं करना पड़ा सो इट इज एज्यूमंगा कि मैं फर्स्ट टाइम इसको एक्सेस कर रहा हूं ओके फर्स्ट टाइम इसको एक्सेस
8:16:528 hours, 16 minutes, 52 secondsकर रहा हूं एंड इट इज आस्किंग फॉर द ईमेल एंड पासवर्ड कि भैया साइन इन करने के लिए यूजर नेम बट मेरा तो भाई अकाउंट ही नहीं है तो मैं क्रिएट अकाउंट पे जा सकता हूं
8:16:598 hours, 16 minutes, 59 secondsसो इट इज आस्किंग ठीक है क्रिएट अकाउंट पे मैं सिंपल ईमेल पासवर्ड पासवर्ड डाल देता हूं एंड लेट्स सी ओके सो वन मोर थिंग डेमो
8:17:078 hours, 17 minutes, 7 secondsईमेल्स नहीं डालनी है एक ऑथेंटिक ईमेल डालना है क्योंकि ईमेल पे वेरिफिकेशन आता है देखो यहां पे कोड भी आता है आपकी ईमेल पे तो ईमेल वेरिफिकेशन भी हो रहा है
8:17:158 hours, 17 minutes, 15 secondsदैट्ची
8:17:278 hours, 17 minutes, 27 secondsअच्छी-अच्छी जो फंक्शनैलिटीज हैं हम लोग कितने आसानी से कर पा रहे हैं यूजिंग द एंपलीफायर ओके
8:17:358 hours, 17 minutes, 35 secondsदैट्ची है सो अभी आप लोग लॉग इन हो चुके हो तो आप काफी आसानी से अभी इसको ऐड कर सकते हो ठीक है इसको डिलीट
8:17:448 hours, 17 minutes, 44 secondsकर सकते हो एंड एट द सेम टाइम आप साइन आउट भी कर सकते हो और साइन इन भी कर सकते
8:17:528 hours, 17 minutes, 52 secondsहो सो मैं यूजर नेम पासवर्ड देता हूं सो मैं काफी आसानी से यहां पे एक्सेस कर पा रहा हूं दिस इज वेरी कूल एंड
8:18:018 hours, 18 minutes, 1 secondयह सारी चीजें जो वेरिफिकेशन है ईमेल एंड एवरीथिंग यह हमारे एडब्ल्यू के रिसोर्सेस से मेंटेन हो रहा है तो अभी तक हम लोग ने
8:18:098 hours, 18 minutes, 9 secondsक्या किया देखो हम लोग ने सैंपल कोड अपने लोकल पे लाया था उसमें चेंजेज कि एंड सो ऑन सो अभी जो हमारे मतलब चेंजेज है लोकली
8:18:178 hours, 18 minutes, 17 secondsप्रेजेंट है हम लोग गेट हब पे अपना कोड अपडेट कर सकते हैं ठीक है उसके लिए हमको क्या करना है चलिए देखते हैं पहले मैं
8:18:248 hours, 18 minutes, 24 secondsइसको कंट्रोल c करके अ स्टॉप कर देता हूं अपने एप्लीकेशन को ठीक है गेट स्टेटस आप देख सकते हो लद देर सो
8:18:338 hours, 18 minutes, 33 secondsमेनी थिंग्स देखो पड t ए एक एंड main.ts ये दो फाइल ही अपनी चेंज हुई है सिंपल हम क्या करेंगे सो गेट कमिट हान एम या और
8:18:438 hours, 18 minutes, 43 secondsयहां पर आप मैसेज लिख सकते हो ठीक है डिलीट फंक्शनल एंड लॉग इन फीचर ठीक है सो मैं इसको कर
8:18:538 hours, 18 minutes, 53 secondsदिया एंड सिंपल हम लोग करेंगे गेट पुश वेरी सिंपल ठीक है गेट पुश कर दिया अब आपको पता है इसके बाद क्या होगा हम जैसे
8:19:028 hours, 19 minutes, 2 secondsही यहां पे जाएंगे मतलब एमप्लीफायर में आप देखोगे डिप्लॉयड हो गया ओके सो यहां पे डिप्लॉयड हो गया कंप्लीट एंड इसको अब
8:19:088 hours, 19 minutes, 8 secondsटेस्ट आउट करते हैं सो हेयर यू गो तो वो देखो सबसे पहले डायरेक्टली साइन इन ही मांग रहा है एंड आफ्टर साइन इन यू कैन
8:19:168 hours, 19 minutes, 16 secondsआल्सो हैव अ दैट फंक्शनालिस और भी चीजें इसमें कवर कर लेते
8:19:268 hours, 19 minutes, 26 secondsहैं कि मान लीजिए बैक एंड में अगर हमको कुछ चेंजेज करने हो तो वह कैसे करेंगे उसके
8:19:328 hours, 19 minutes, 32 secondsलिए हमको लोकल एडब्ल्यू एस क्रेडेंशियल को सेटअप करना पड़ेगा यह चीज हम लोग ने आईएएम मैनेजमेंट वाले वीडियो पे कवर कर चुका है
8:19:428 hours, 19 minutes, 42 secondsअगर आपको देखना है तो देख सकते हो लद यह जो है देखो कॉन्फिडो लोकल डेवलपमेंट इसमें लिंक दी हुई है आप उसको भी फॉलो कर सकते
8:19:498 hours, 19 minutes, 49 secondsहो लद मेरा वीडियो देख लो ज्यादा इजी है है ओके जो भी यूजर बनाओगे आईएम में उसमें क्या है एमप्लीफाई बैक एंड डिप्लॉयड उसको
8:19:588 hours, 19 minutes, 58 secondsये परमिशन होनी चाहिए सो दैट वो ये चेंजेज कर पाए ट्स इट एंड उसके बाद वो कीज वगैरह आप जनरेट करते हो टोकस एंड एड्स कॉन्फिन
8:20:068 hours, 20 minutes, 6 secondsकरते हो ओके एंड आपको चेक करना है अगर कि ल मतलब यह आप रेडी टू यूज होगी नहीं तो आप
8:20:138 hours, 20 minutes, 13 secondsसिंपली क्या करो एडब्ल्यू एस ठीक है गेट कॉलर आइडेंटिटी एसटीएस ऐसे कर सकते हो अगर आपको इस टाइप का रिजल्ट मिलता है दैट मींस
8:20:218 hours, 20 minutes, 21 secondsएडब्ल्यू क्रेडेंशियल इट इट इज वर्किंग फाइन ओके इसके बाद हम लोग क्या चेंजेज
8:20:288 hours, 20 minutes, 28 secondsकरने वाले हैं बैक एंड में पहले तो मैं वह बता देता हूं अभी जो अभी हमारा जो ऐप है उसमें क्या होगा कि जो मल्टीपल यूजर्स हैं
8:20:358 hours, 20 minutes, 35 secondsमान लो हमारा साइन इन लॉगइन वाला तो हो गया काम मान लो दो यूजर ने अगर उस पर अकाउंट बनाया लॉग इन किया दोनों यूजर को
8:20:428 hours, 20 minutes, 42 secondsजो टू डू लिस्ट है वो सेम दिखेगी ठीक है अगर आप बोलोगे कि अरे ये क्या फायदा हुआ मतलब हर यूजर की अपनी टू डू लिस्ट होना
8:20:498 hours, 20 minutes, 49 secondsचाहिए ठीक है तो वो अपडेट अभी हम लोग करेंगे है ना तो वो चीज हम लोग करने वाले हैं ओके एडब्ल्यू एस क्रेडेंशियल आपके पास
8:20:578 hours, 20 minutes, 57 secondsहोना चाहिए एंड इसके बाद एक कांसेप्ट और आया है जो कि है क्लाउड सैड बॉक्स का अभी आपने य आप लोग ने नोटिस किया होगा
8:21:078 hours, 21 minutes, 7 secondsकि जब आपने इस ऐप को क्लोन किया एंड और आउटपुट फाइल यूज करी आप जो भी चेंज कर रहे
8:21:158 hours, 21 minutes, 15 secondsथे वो डायरेक्टली जो मतलब मेन एप्लीकेशन के जो बैक एंड था मतलब डेटा बेस था उसमें अपडेट हो रहा था लेकिन ऐसा होना नहीं
8:21:238 hours, 21 minutes, 23 secondsचाहिए क्योंकि अगर आप टेस्टिंग ही कर रहे हो कुछ कुछ काफी सारी चीजें तो मेन पे इसका इंपैक्ट नहीं होना चाहिए सेकंड चीज दो यूजर तीन यूजर साथ में टेस्टिंग कर रहे
8:21:328 hours, 21 minutes, 32 secondsहो अगर उनको करना हो सो व्हाट इज द मतलब क्या उसका तरीका है क्या एक सॉल्यूशन है
8:21:408 hours, 21 minutes, 40 secondsसो एक सैंड बॉक्स मतलब एक वर्चुअल मतलब सेम रप्ल का आपका एक एप्लीकेशन बन जाएगा लेकिन आपके मेन बैक एंड और डेटाबेस से
8:21:498 hours, 21 minutes, 49 secondsकनेक्ट नहीं होगा वो करके देखते हैं उसके लिए सिंपली आपको पता क्या करना है इट्स वेरी सिंपल ये कमांड यूज करनी है एनपी एक
8:21:568 hours, 21 minutes, 56 secondsएमपी एक सैंड बॉक्स वेरी सिंपल ठीक है इसको मैं करता हूं और आपको दिखाता हूं वेरी इंटरेस्टिंग चीज ओके एंड उसके पहले
8:22:048 hours, 22 minutes, 4 secondsमैं क्या है जो एंपलीफायर आउटपुट वाली जो फाइल है उसको ओपन करके रखता हूं इसके बाद मैं ये वाली कमांड एग्जीक्यूट करता हूं
8:22:118 hours, 22 minutes, 11 secondsएनपीए सैंड बॉक्स वाली एंड ये भी हमको इसलिए चाहिए क्योंकि हम लोग यहां पे एक सैंड बॉक्स बना रहे हैं विद इन अवर
8:22:188 hours, 22 minutes, 18 secondsएडब्ल्यू कंसोल सो ओके सो यहां पे सैंड बॉक्स का सेटअप हो गया है कंप्लीट और यहां पे आप एक चीज नोटिस करोगे सैंड बॉक्स वाचिंग फॉर फाइल चेंजेज मतलब अब अपन
8:22:278 hours, 22 minutes, 27 secondsचेंजेज करना स्टार्ट कर सकते हैं एंड अगर आप देखोगे फाइल रिटर्न तो एमप्लीफाई आउटपुट मतलब जो आपकी प्रीवियस आउटपुट फाइल
8:22:368 hours, 22 minutes, 36 secondsथी वो उसने ओवरराइड कर दी है मतलब ये आपके अलग पूरे रिसोर्सेस बना दिए हैं तो आप जो भी टेस्टिंग करोगे जो भी चीजें करोगे वह
8:22:448 hours, 22 minutes, 44 secondsआपके मेन वाले से कनेक्ट नहीं होगा मतलब अपडेट नहीं होगा चलिए डॉक्यूमेंटेशन प
8:22:518 hours, 22 minutes, 51 secondsचलते हैं और इसके बैक एंड में अब चेंज करते हैं तो बैक एंड में हम लोग क्या चेंजेज करने वाले हैं इंप्लीमेंट पर यूजर
8:22:578 hours, 22 minutes, 57 secondsऑथराइजेशन मतलब कि हर एक यूजर के अपने अकाउंट हो और अपनी अलग टू डो लिस्ट हो ओके
8:23:048 hours, 23 minutes, 4 secondsसिंपल सा एक चीज है है ना तो एक काम करते हैं इसमें कौन सी फाइल में चेंज करना है तो आप देख सकते हो एमप्लीफाई के अंदर डेटा
8:23:128 hours, 23 minutes, 12 secondsएंड रिसोर्स डटी ए ठीक है चलिए एमप्लीफाई है और इसमें डाटा और रिसोर्स टीए टाइप
8:23:218 hours, 23 minutes, 21 secondsस्क्रिप्ट है ठीक है तो ये फाइल हम लोग ने ओपन कर ली इसको थोड़ा सा ऐसा कर लेता हूं ठीक ठीक है इसमें हम लोग को क्या करना है
8:23:288 hours, 23 minutes, 28 secondsकॉपी करना है एक ये कोड सो यह कोड हमारा यहां से होगा ठीक है ये वाला इतना और यहां
8:23:368 hours, 23 minutes, 36 secondsपे मैं पेस्ट करता हूं सेव करता हूं ये एक हो गया और रिसोर्स टीए में एक और हमारा चेंज क्या है कॉपी
8:23:448 hours, 23 minutes, 44 secondsएंड डिफॉल्ट ऑथराइजेशन मोड क्या है यूजर पूल ओके एंड इसमें हम लोग देख लेते हैं कहां पे है देखो यहां पे आप देखोगे
8:23:538 hours, 23 minutes, 53 secondsडिफॉल्ट मोड क्या है एपीआई की है तो इसके जगह हम लोग क्या कर देते यूजर पोर्ट ठीक है सो ठीक है तो यह पूरी एक लाइन ही है यह
8:24:028 hours, 24 minutes, 2 secondsहमको पूरी लाइन चेंज करना है तो मैं बोल देता हूं यूजर पूल ओके इसको मैं सेव करता हूं ठीक है इसके बाद आ जाते हैं सोर्स में
8:24:118 hours, 24 minutes, 11 secondsडटी एक ओके सो इसमें क्या-क्या चेंजेज है लेट्स सी सोर्स में हम लोग ने ऑलरेडी भी चेंज किया था एटीए एक पे आ जाते हैं ठीक
8:24:218 hours, 24 minutes, 21 secondsहै एंड इसमें क्या-क्या चेंजेज है देखो कांस्टेंट था यूजर एंड साइन आउट ठीक है अब
8:24:288 hours, 24 minutes, 28 secondsऑथेंटिकेटर यूज कर रहे थे अब यहां पे सिर्फ साइन आउट था पहले अभी यहां पे यूजर हम लोग को ऐड करना है लद मैं सिंपल यहां
8:24:368 hours, 24 minutes, 36 secondsपे यूजर और साइन आउट ठीक है तो इस तरह से मैंने ऐड कर दिया ओके सो यहां पे हेडर वन है ठीक है h1 सो मैं इसको कॉपी करता हूं एंड ये जो .
8:24:518 hours, 24 minutes, 51 secondsts2 डज है तो इसके जगह हम लोग ये रिप्लेस कर देते हैं एंड इसको सेव करता हूं सो ठीक है ये फॉर्मेटिंग हो जाएगी सो इसमें हम
8:24:598 hours, 24 minutes, 59 secondsलोग क्या कर रहे हैं यह जो भी यूजर लॉग इन होगा उसका टू डू लिस्ट मतलब उस एक पर्टिकुलर यूजर की लिस्ट होनी चाहिए ओके
8:25:088 hours, 25 minutes, 8 secondsसो यहां पे हमारे सारे चेंजेज तो हो गए लेकिन अपने ऐप को रन करने के लिए क्या करना पड़ेगा एनपीएम रन डेव तो इसके लिए मैं सिंपली क्या करता हूं एक और यहां पे
8:25:168 hours, 25 minutes, 16 secondsटर्मिनल ओपन कर लेता हूं एंड इसके ऐप के अंदर चले जाते हैं एंड सिंपली करता हूं मैं एनपीएम रन डेफ वेरी सिंपल ठीक है तो
8:25:258 hours, 25 minutes, 25 secondsये हमारा स्टार्ट हो गया एंड इसके बाद हम लोग ब्राउजर प आते हैं सिंपली रीलोड करते हैं सो यू विल सी कि अभी हम लोग फिर से
8:25:328 hours, 25 minutes, 32 secondsउसी अकाउंट में आ गए हैं लोकल होस्ट 5173 अभी हम लोग ने सैंड बॉक्स प आ गए हैं ठीक है तो पुराना जो हमारे पास डाटा था या
8:25:398 hours, 25 minutes, 39 secondsतो हम लोग ने यूजर क्रिएट किया था वोह हमारे पास नहीं है यह कंपलीटली एक नया सेट ऑफ है तो हम लोग को ऑफकोर्स एक मतलब
8:25:468 hours, 25 minutes, 46 secondsक्रिएट अकाउंट करना पड़ेगा ठीक है तो फिर से मैं एक और अकाउंट क्रिएट कर रहा हूं सिंपल एंड कंफर्म एंड हेयर यू गो सो यहां पे एक चीज
8:25:568 hours, 25 minutes, 56 secondsआप हमने इस बार डिफरेंट देखी वो यह क्या थी कि भाई देखो यह जो टू डू लिस्ट है वो इस पर्सन की है मतलब आप इसमें यूजर नेम या
8:26:048 hours, 26 minutes, 4 secondsतो आप सिंपली ईमेल अपन ने यूज़ कर लिए ठीक है बाकी ये सब चीजें यहां पे चलेगी सिंपल ठीक है
8:26:268 hours, 26 minutes, 26 secondsबॉक्स कैसे यूज करना वो भी अभी हम लोग ने देख लिया ओके सो यहां पे हम लोग ने वो चेंजेज भी कर लिए सैंड बॉक्स कैसे यूज करना उसका भी हम लोग ने एक आईडिया देख
8:26:348 hours, 26 minutes, 34 secondsलिया एंड हां चलो मेरे को एक चीज और दिखानी थी कि अभी हम लोग ने देखो चेंजेज किए कुछ और मैं ऐप में चलता हूं और मैं
8:26:418 hours, 26 minutes, 41 secondsआपको यहां पे दिखाता हूं मेन में चलते हैं एंड इसमें डाटा पे चलता हूं एंड लेट्स सी
8:26:488 hours, 26 minutes, 48 secondsऔर यहां पे डटा मैनेजर डटा मैनेजर पे आ ग ऐप मतलब यहां पे कुछ अलग है लिस्ट और ये
8:26:598 hours, 26 minutes, 59 secondsजो हमारा मेन वाला है इसमें कोई भी चेंजेज नहीं हुए हैं हां लेकिन इसमें मैं एक चीज और दिखाना चाहता हूं कि अभी ऑल एप्स पे आए
8:27:078 hours, 27 minutes, 7 secondsये आ गया हमारा स्टैटिक वेबसाइट और हमारा एंपलीफायर रिएक्ट वाला ऐप यहां पे आप देखोगे मैनेज सैंड बॉक्सेस आ गया अब यहां
8:27:168 hours, 27 minutes, 16 secondsपे सैंड बॉक्स पे आओगे आप देखोगे एक सैंड बॉक्स यहां पे ऑलरेडी दिया हुआ है तो हम लोग ने कोड में तो चेंज किया है चलो अब हम
8:27:228 hours, 27 minutes, 22 secondsलोग उस कोड को ठीक है कमिट करके पुश भी कर देते हैं मैं सिंपल कंट्रोल सी करता हूं गेट ऐड क्योंकि हम लोग चेंजेज को ऐड कर
8:27:318 hours, 27 minutes, 31 secondsलें गेट कमिट हाइफ ए एम यूजर थ मतलब मैं
8:27:388 hours, 27 minutes, 38 secondsसिंपली कर रहा हूं अभी गेट पुश तो हम लोग के सारे चेंजेज यहां पे गिट पुश हो जाएंगे एंड यहां पे कोई डिफरेंस नहीं आएगा
8:27:468 hours, 27 minutes, 46 secondsक्योंकि सैंड बॉक्स कैसे काम करता है सैंड बॉक्स सिर्फ काम करता है एमप्लीफाई आउटपुट फाइल के बेसिस पे वो तो हम कमिट कर ही
8:27:548 hours, 27 minutes, 54 secondsनहीं रहे हैं ठीक है तो हम लोग ने सिर्फ कोड में चेंजेज किया वो भी मेन में और आप देखोगे जैसे कि हम लोग ने डिप्लॉयडी पलय मेंट हो गया कंप्लीट एंड एक
8:28:038 hours, 28 minutes, 3 secondsबार हम लोग इसको चेक कर लेते हैं सो अगेन इट इज आस्किंग फॉर द साइन
8:28:108 hours, 28 minutes, 10 secondsइन सो मैं साइन इन जैसे ही करता हूं एंड सो मुझे यहां पे अपना ही एक अकाउंट दिखता
8:28:178 hours, 28 minutes, 17 secondsहै एंड दैट इज रियली कूल सो दिस इज हाउ इट वर्क्स एंड आई कैन आल्सो
8:28:288 hours, 28 minutes, 28 secondsसाइन आउट यहां पे देखोगे ये ये देखो यह वाला जो था मतलब जो चेंजेज वो तो आ गया एंड यहां पे साइन आउट का बटन है एंड आल्सो
8:28:368 hours, 28 minutes, 36 secondsडिलीट वाला ऑप्शन भी है ऑलराइट सो अभी तक हम लोग ने एंपलीफायर में पूरा प्रैक्टिकल करके देख लिया है सो
8:28:448 hours, 28 minutes, 44 secondsसेक्शन के एंड में हम लोग क्लीन अप करेंगे ऑफकोर्स इतनी सारी चीजें बनाई है तो हम लोग उसको रिमूव कर देंगे सो दैट काफी
8:28:528 hours, 28 minutes, 52 secondsज्यादा ऐसे बिलिंग ना आए पहले तो हम लोग सैंड बॉक्स प चलते हैं ओके एंड जो हमारा सैंड बॉक्स है उसको डिलीट करते हैं ओके सो
8:29:008 hours, 29 minutesइसको मैं सेलेक्ट करता हूं एंड डिलीट सिलेक्टेड एंड डिलीट कंफर्म सो विल बी डिलीटेड फ्रॉम
8:29:088 hours, 29 minutes, 8 secondsद एडब्ल्यू एस क्लाउड फॉर्मेशन फ्यू मिनट लगेंगे एंड सी यहां पे डिलीट का ऑप्शन आ गया है ऑल एप्स पे हम लोग चलेंगे जो हमारे
8:29:158 hours, 29 minutes, 15 secondsमेन एप्स हैं माय स्टैटिक वेबसाइट पे चलते हैं जो हमारी पुरानी वेबसाइट थी ऐप सेटिंग्स में आ जाओ जनरल सेटिंग्स एंड
8:29:228 hours, 29 minutes, 22 secondsयहां देखोगे डिलीट ऐप एंड यहां पे सिंपल डिलीट ऐप पे मैं क्लिक करूंगा सिंपल यहां पर डिलीट आपको टाइप करना है डिलीट पे करना
8:29:308 hours, 29 minutes, 30 secondsहै एंड आपका ऐप थोड़ी देर में डिलीट हो जाएगा आप देख सकते हो डिलीट हो भी गया एंड सिमिलरली हम लोग यह वाला जो रिएक्ट वाला
8:29:398 hours, 29 minutes, 39 secondsऐप था इसको भी हम लोग डिलीट कर देंगे ऑल दो आप प्ले अराउंड और करना चाहते हो कुछ और चेंजेज करना चाहते हो सो देख सकते हो लद मैं अभी इसको डिलीट
8:29:488 hours, 29 minutes, 48 secondsकरूंगा एंड यह जैसे कि ऐप आप लोग ने डिलीट किया ठीक है ऐप डिलीट करने के साथ-साथ अगर
8:29:558 hours, 29 minutes, 55 secondsमैं आपको से जैसे कि दिखाता हूं डायनेमो डीवी दिखाता हूं ठीक है कि भाई वहां से सब टेबल मेबल हट गए कि नहीं
8:30:048 hours, 30 minutes, 4 secondsयहां पर डायनेमो डीवीवी मतलब उसके रिलेटेड जो भी चीजें थी वो यहां पे क्लीन हो चुकी है ठीक है एक चीज हो गई और मेरे केस में
8:30:118 hours, 30 minutes, 11 secondsजो कि मैंने होस्टेड जोन बनाया था सो मैं वो चीज भी यहां पर हटा देता हूं राउट 53 एंड देखो जो डीएनएस जो था हमारा यह
8:30:218 hours, 30 minutes, 21 secondsहोस्टेड जोन यह वाला एंड जो ये देखो ये जितने भी रिकॉर्ड्स बने हुए हैं यह रिकॉर्ड्स हैं
8:30:288 hours, 30 minutes, 28 secondsऔर एक चीज और थी यह जो सी नेम वाला जो रिकॉर्ड है वो ये एंपलीफायर वाले ने बनाया
8:30:368 hours, 30 minutes, 36 secondsथा एंड जो सी नेम रिकॉर्ड है उसको डिलीट करने के बाद हम लोग डायरेक्टली जो होस्टेड जन है उसको डिलीट कर सकते हैं काफी आसानी से सो यू कैन सी कि होस्टेड जोन भी हमारा
8:30:468 hours, 30 minutes, 46 secondsडिलीट हो चुका है सिर्फ मेरे पास अभी डोमेन रजिस्टर्ड है ऑल राइट सो दिस इज ऑल अबाउट द एडब्ल्यू एंपलीफायर होप ये वीडियो
8:30:538 hours, 30 minutes, 53 secondsआपको पसंद आई होगी एंड मिलते हैं नेक्स्ट से सेक्शन में किसी एक और टॉपिक के साथ ऑलराइट सो आज की वीडियो में हम लोग

```
