import matplotlib.pyplot as plt

# Data from your table
latencies = [100, 90, 70, 50, 30, 10]  # in ms
recall = [100.0, 99.7, 99.6, 99.3, 98.6, 97.6]

# Create figure and axis
fig, ax1 = plt.subplots(figsize=(8, 5))

# Reverse x-axis (since higher latency = slower)
ax1.invert_xaxis()

# Add a second y-axis for Recall
ax1.plot(latencies, recall, "s--", label="Recall (%)")
ax1.set_xlabel("Max Latency (ms)", fontsize=12)
ax1.set_ylabel("Recall (%)", fontsize=12)
ax1.tick_params(axis="y")

# Title and grid
plt.title("Impact of Latency Bound on Recall", fontsize=14, pad=15)
ax1.grid(True, linestyle="--", alpha=0.6)

# Add legend combining both axes
lines_1, labels_1 = ax1.get_legend_handles_labels()
plt.legend(lines_1, labels_1, loc="lower left")

# Tight layout
plt.tight_layout()
plt.show()
