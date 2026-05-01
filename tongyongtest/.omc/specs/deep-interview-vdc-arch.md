# Deep Interview Spec: 金桥基地VDC业务平台产品架构设计

## Metadata
- Interview ID: vdc-arch-interview-001
- Rounds: 3
- Final Ambiguity Score: 15.3%
- Type: greenfield
- Generated: 2026-04-14
- Threshold: 0.2
- Status: PASSED

## Clarity Breakdown
| Dimension | Score | Weight | Weighted |
|-----------|-------|--------|----------|
| Goal Clarity | 0.88 | 0.40 | 0.352 |
| Constraint Clarity | 0.80 | 0.30 | 0.240 |
| Success Criteria | 0.85 | 0.30 | 0.255 |
| **Total Clarity** | | | **0.847** |
| **Ambiguity** | | | **15.3%** |

## Goal
Produce a detailed product architecture design document for the 金桥基地VDC智能算法项目业务平台, based on the business requirements spec and PDI logic confirmation document. The document must cover both high-level conceptual architecture and implementation-ready technical details across 8 standard sections.

## Constraints
- Target deployment: on-premise physical server
- Network: stable LAN/VPN connectivity between both sites (金桥库, 凯迪库) and central server
- Technology preference: open-source / cost-effective stack
- Scale: 10 concurrent users, 3s alert latency, 5s complex report query
- Data retention: alarm records + images 90 days, raw state stream 30 days
- Site-level data isolation required

## Non-Goals
- Actual software implementation (architecture document only)
- Edge box algorithm development (box is external system)
- Video storage/playback (NVR handles this)
- SSO integration (optional, not required in architecture)

## Acceptance Criteria
- [ ] Contains overall architecture diagram
- [ ] Contains module breakdown and interaction descriptions
- [ ] Contains technology stack with rationale
- [ ] Contains data model / database design
- [ ] Contains interface definitions (box-to-platform protocols, internal APIs)
- [ ] Contains deployment topology for on-premise + 2-site setup
- [ ] Contains security and permission design
- [ ] Contains performance and scalability analysis

## Ontology (Key Entities)
| Entity | Type | Fields | Relationships |
|--------|------|--------|---------------|
| Business Platform | core domain | alerts, reports, users, devices, configs | receives data from Edge Box |
| Edge Computing Box | external system | box_id, site, ip, status, channels | pushes state streams to Business Platform |
| PDI Workstation | core domain | workstation_name, standard_duration, threshold | generates reports and alerts |
| State Stream | core domain | door_open, person_present, person_entering_exiting, timestamp | input to Business Rule Engine |
| Business Rule Engine | core domain | enter_rule, exit_rule, duration_calc, compliance_check | processes State Stream |
| Alert/Warning | core domain | type, site, channel, timestamp, images, status | output from Rule Engine / Box events |
| Report | supporting | site, vehicle, workstation, duration, deviation, result | output from PDI processing |
| Site | supporting | site_name, isolation_policy | contains Boxes, Channels, Users |
| Channel/Camera | supporting | channel_id, type, algorithm, status | belongs to Edge Box |
| User/Role | supporting | username, role, site_permissions, functional_permissions | accesses Platform data |

## Interview Transcript
<details>
<summary>Full Q&A (3 rounds)</summary>

### Round 1
**Q:** What should the architecture document primarily focus on? / Are there any technology constraints?
**A:** Both high-level + technical depth. Prefer open-source / cost-effective stack.

### Round 2
**Q:** How should we verify the architecture document is complete?
**A:** Must cover standard sections: overall architecture diagram, module breakdown, technology stack with rationale, data model, interface definitions, deployment topology, security/permission design, performance/scalability.

### Round 3
**Q:** What is the target deployment environment? Any network or compliance constraints?
**A:** On-premise physical server. Sites have stable LAN/VPN to central server.
</details>
