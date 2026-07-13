export function splitUserActivities(activities = []) {
  return activities.reduce((groups, activity) => {
    if (activity.joined) groups.mine.push(activity)
    else if (activity.canJoin) groups.available.push(activity)
    else groups.unavailable.push(activity)
    return groups
  }, { mine: [], available: [], unavailable: [] })
}
