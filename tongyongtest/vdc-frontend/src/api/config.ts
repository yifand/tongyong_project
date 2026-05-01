import { get, put } from './request'
import type { RuleConfig, SystemConfig } from '@/types'

export function getRuleConfigs(): Promise<RuleConfig[]> {
  return get('/api/v1/config/rules')
}

export function updateRuleConfigs(data: RuleConfig[]): Promise<void> {
  return put('/api/v1/config/rules', data)
}

export function getThresholdConfigs(): Promise<SystemConfig[]> {
  return get('/api/v1/config/thresholds')
}

export function updateThresholdConfigs(data: SystemConfig[]): Promise<void> {
  return put('/api/v1/config/thresholds', data)
}

export function getGeneralConfigs(): Promise<SystemConfig[]> {
  return get('/api/v1/config/general')
}

export function updateGeneralConfigs(data: SystemConfig[]): Promise<void> {
  return put('/api/v1/config/general', data)
}
